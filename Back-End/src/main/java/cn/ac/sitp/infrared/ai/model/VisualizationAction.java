package cn.ac.sitp.infrared.ai.model;

import java.util.HashMap;
import java.util.Map;

/**
 * A frontend visualization command dispatched via postMessage to Cesium viewer.
 */
public class VisualizationAction {

    private String type;  // LOAD_IMAGE, FLY_TO, CLEAR_MAP, HIGHLIGHT, SHOW_TABLE
    private Map<String, Object> params = new HashMap<>();

    public VisualizationAction() {}

    public VisualizationAction(String type) {
        this.type = type;
    }

    public VisualizationAction param(String key, Object value) {
        this.params.put(key, value);
        return this;
    }

    public static VisualizationAction flyTo(double longitude, double latitude) {
        return new VisualizationAction("FLY_TO")
                .param("longitude", longitude)
                .param("latitude", latitude);
    }

    public static VisualizationAction loadImage(String imgAddr, Object leftUpPos, Object rightBottomPos, Object id) {
        return new VisualizationAction("LOAD_IMAGE")
                .param("imgAddr", imgAddr)
                .param("leftUpPos", leftUpPos)
                .param("rightBottomPos", rightBottomPos)
                .param("id", id);
    }

    public static VisualizationAction clearMap() {
        return new VisualizationAction("CLEAR_MAP");
    }

    public static VisualizationAction highlight(Object id) {
        return new VisualizationAction("HIGHLIGHT").param("id", id);
    }

    public static VisualizationAction showTable(Object data) {
        return new VisualizationAction("SHOW_TABLE").param("data", data);
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Map<String, Object> getParams() { return params; }
    public void setParams(Map<String, Object> params) { this.params = params; }
}
