package cn.ac.sitp.infrared.ai.agent;

import cn.ac.sitp.infrared.ai.model.ChatResponse;
import cn.ac.sitp.infrared.ai.model.VisualizationAction;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Agent for generating frontend visualization commands.
 * Does not call LLM — translates tool calls from PlanExecutor into VisualizationActions.
 */
@Component
public class VisualizationAgent {

    private static final Logger log = LoggerFactory.getLogger(VisualizationAgent.class);

    /**
     * Execute a visualization tool and return actions for the frontend.
     */
    public ChatResponse executeTool(String toolName, String argsJson, Map<String, Object> context) {
        JSONObject args = argsJson != null && !argsJson.isBlank()
                ? JSON.parseObject(argsJson) : new JSONObject();

        ChatResponse response = new ChatResponse();

        switch (toolName) {
            case "fly_to" -> {
                double lon = args.getDoubleValue("longitude");
                double lat = args.getDoubleValue("latitude");
                response.addAction(VisualizationAction.flyTo(lon, lat));
                response.setReply("正在将地图定位到指定位置。");
            }
            case "show_on_map" -> {
                String imgAddr = args.getString("imgAddr");
                Object leftUpPos = args.get("leftUpPos");
                Object rightBottomPos = args.get("rightBottomPos");
                Object id = args.get("id");
                response.addAction(VisualizationAction.loadImage(imgAddr, leftUpPos, rightBottomPos, id));
                response.setReply("正在地图上加载影像数据。");
            }
            case "clear_map" -> {
                response.addAction(VisualizationAction.clearMap());
                response.setReply("已清除地图上的所有图层。");
            }
            case "highlight" -> {
                Object id = args.get("id");
                response.addAction(VisualizationAction.highlight(id));
                response.setReply("已高亮显示指定要素。");
            }
            case "show_table" -> {
                // Data comes from context (previous step results)
                Object data = context != null ? context.get("lastResult") : null;
                if (data != null) {
                    response.addAction(VisualizationAction.showTable(data));
                }
                response.setReply("数据已展示在表格中。");
            }
            default -> response.setReply("未知的可视化操作: " + toolName);
        }

        return response;
    }

    /**
     * Generate visualization actions from search results.
     * Examines data for geographic info and creates appropriate map actions.
     * For NC data: parses ncGeometry (GeoJSON Polygon) to extract bounding box,
     * generates LOAD_IMAGE with {lon, lat} positions matching the frontend format.
     */
    @SuppressWarnings("unchecked")
    public List<VisualizationAction> suggestActionsForData(Map<String, Object> data) {
        List<VisualizationAction> actions = new java.util.ArrayList<>();

        if (data == null) return actions;

        Object list = data.get("ncList");
        if (list == null) list = data.get("list");
        if (!(list instanceof List<?> dataList) || dataList.isEmpty()) return actions;

        // Show data in table
        actions.add(VisualizationAction.showTable(list));

        // Process first item for map visualization
        if (dataList.getFirst() instanceof Map<?, ?> firstItem) {
            Map<String, Object> item = (Map<String, Object>) firstItem;

            // Try NC data: ncGeometry is a GeoJSON string like {"type":"Polygon","coordinates":[[[lon,lat],...]]]}
            Object ncGeometryObj = item.get("ncGeometry");
            if (ncGeometryObj instanceof String geoStr && !geoStr.isBlank()) {
                try {
                    JSONObject geo = JSON.parseObject(geoStr);
                    com.alibaba.fastjson2.JSONArray coords = geo.getJSONArray("coordinates");
                    if (coords != null && !coords.isEmpty()) {
                        com.alibaba.fastjson2.JSONArray ring = coords.getJSONArray(0); // outer ring
                        if (ring != null && ring.size() >= 3) {
                            // Extract bounding box from polygon ring
                            // Frontend expects: leftUpPos = {lon: minLon, lat: maxLat}, rightBottomPos = {lon: maxLon, lat: minLat}
                            double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
                            double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
                            for (int i = 0; i < ring.size(); i++) {
                                com.alibaba.fastjson2.JSONArray point = ring.getJSONArray(i);
                                if (point != null && point.size() >= 2) {
                                    double lon = point.getDoubleValue(0);
                                    double lat = point.getDoubleValue(1);
                                    minLon = Math.min(minLon, lon);
                                    maxLon = Math.max(maxLon, lon);
                                    minLat = Math.min(minLat, lat);
                                    maxLat = Math.max(maxLat, lat);
                                }
                            }

                            Object ncId = item.get("id");
                            String imgAddr = "/infrared/rest/nc/file/" + ncId;

                            // Positions must be {lon, lat} objects to match frontend loadImgResult()
                            Map<String, Double> leftUpPos = Map.of("lon", minLon, "lat", maxLat);
                            Map<String, Double> rightBottomPos = Map.of("lon", maxLon, "lat", minLat);

                            actions.add(VisualizationAction.loadImage(imgAddr, leftUpPos, rightBottomPos, ncId));

                            // Fly to center
                            double centerLon = (minLon + maxLon) / 2;
                            double centerLat = (minLat + maxLat) / 2;
                            actions.add(VisualizationAction.flyTo(centerLon, centerLat));
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse ncGeometry for visualization: {}", geoStr, e);
                }
            } else {
                // Try natural disaster data: latitude/longitude fields
                Double lat = toDouble(item.get("latitude"));
                Double lon = toDouble(item.get("longitude"));
                if (lat != null && lon != null) {
                    actions.add(VisualizationAction.flyTo(lon, lat));
                }
            }
        }

        return actions;
    }

    private Double toDouble(Object obj) {
        if (obj instanceof Number n) return n.doubleValue();
        if (obj instanceof String s) {
            try { return Double.parseDouble(s); } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
