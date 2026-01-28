let drawHandler = null;
let activeShapePoints = [];
let floatingPoint;
let activeShape;
let lastFeature;
const drawLayers = [];
/**
 * 手动绘制几何图形
 * @param viewer - Cesium 地图视图对象
 * @param drawingMode - 绘制模式：点/线/面/圆/矩形
 * @param options.removeLast - 是否清除上一个绘制
 * @param callback - 绘制完成回调函数
 * @returns 屏幕空间事件处理器
 */
export const draw = (viewer, drawingMode, options = { removeLast: true }, callback) => {
    if (!viewer)
        return;
    const { removeLast } = options;
    // 配置场景参数
    viewer.scene.globe.depthTestAgainstTerrain = false;
    viewer.enableCursorStyle = false;
    viewer._element.style.cursor = 'crosshair';
    // 清理现有处理器
    if (drawHandler && !drawHandler.isDestroyed()) {
        drawHandler.destroy();
    }
    terminateShape();
    // 初始化事件处理器
    drawHandler = new Cesium.ScreenSpaceEventHandler(viewer.canvas);
    // 处理左键点击事件
    drawHandler.setInputAction((event) => {
        const earthPosition = viewer.scene.pickPosition(event.position);
        if (Cesium.defined(earthPosition)) {
            if (drawingMode === 'Point') {
                // 点要素处理逻辑
                const finalPoint = createPoint(earthPosition);
                drawLayers.push(finalPoint);
                cleanupHandler();
                callback === null || callback === void 0 ? void 0 : callback(finalPoint);
                return;
            }
            if (!activeShapePoints.length) {
                // 初始化动态图形
                floatingPoint = createPoint(earthPosition, false);
                activeShapePoints.push(earthPosition);
                const dynamicPositions = new Cesium.CallbackProperty(() => {
                    return drawingMode === 'Polygon' ? new Cesium.PolygonHierarchy(activeShapePoints) : activeShapePoints;
                }, false);
                activeShape = drawShape(dynamicPositions);
            }
            else if (['Rectangle', 'Circle'].includes(drawingMode)) {
                // 矩形和圆特殊处理
                cleanupHandler();
                terminateShape(removeLast);
                return;
            }
            activeShapePoints.push(earthPosition);
        }
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK);
    // 处理鼠标移动事件
    drawHandler.setInputAction((event) => {
        var _a;
        if (floatingPoint) {
            const newPosition = viewer.scene.pickPosition(event.endPosition);
            if (Cesium.defined(newPosition)) {
                (_a = floatingPoint.position) === null || _a === void 0 ? void 0 : _a.setValue(newPosition);
                activeShapePoints.pop();
                activeShapePoints.push(newPosition);
            }
        }
    }, Cesium.ScreenSpaceEventType.MOUSE_MOVE);
    // 处理双击事件
    drawHandler.setInputAction(() => {
        cleanupHandler();
        activeShapePoints = activeShapePoints.slice(0, -2);
        terminateShape(removeLast);
    }, Cesium.ScreenSpaceEventType.LEFT_DOUBLE_CLICK);
    /** 清理事件处理器 */
    const cleanupHandler = () => {
        drawHandler === null || drawHandler === void 0 ? void 0 : drawHandler.destroy();
        viewer._element.style.cursor = 'default';
    };
    /** 终止当前图形绘制 */
    function terminateShape(removeLast) {
        removeLast && viewer.entities.remove(lastFeature);
        if (activeShapePoints.length) {
            const finalShape = drawShape(activeShapePoints, true);
            drawLayers.push(finalShape);
            callback === null || callback === void 0 ? void 0 : callback(finalShape);
        }
        viewer.entities.remove(floatingPoint);
        viewer.entities.remove(activeShape);
        floatingPoint = undefined;
        activeShape = undefined;
        activeShapePoints = [];
    }
    /** 创建点要素 */
    function createPoint(worldPosition, isPoint = true) {
        const point = viewer.entities.add({
            position: worldPosition,
            point: {
                outlineWidth: isPoint ? 2 : 0,
                outlineColor: Cesium.Color.fromBytes(51, 153, 204),
                color: isPoint ? Cesium.Color.WHITE.withAlpha(0.5) : Cesium.Color.TRANSPARENT,
                pixelSize: 10
            }
        });
        point.positionData = worldPosition;
        return point;
    }
    /** 计算两点间距离（平面坐标系） */
    function calcRadius(point1, point2) {
        return Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
    }
    /** 绘制几何图形 */
    function drawShape(positionData, final = false) {
        let shape;
        switch (drawingMode) {
            case 'Polyline':
                shape = viewer.entities.add({
                    polyline: {
                        positions: positionData,
                        material: Cesium.Color.fromBytes(51, 153, 204)
                    }
                });
                shape.positionData = positionData;
                break;
            case 'Polygon':
                shape = viewer.entities.add({
                    polygon: {
                        hierarchy: positionData,
                        perPositionHeight: true,
                        material: Cesium.Color.WHITE.withAlpha(0.5),
                        outline: true,
                        outlineColor: Cesium.Color.fromBytes(51, 153, 204)
                    }
                });
                shape.positionData = positionData;
                break;
            case 'Circle': {
                const positions = positionData instanceof Cesium.CallbackProperty
                    ? positionData.getValue(Cesium.JulianDate.now())
                    : positionData;
                const radius = calcRadius(positions[0], positions[positions.length - 1]);
                const callbackRadius = new Cesium.CallbackProperty(() => calcRadius(positions[0], positions[positions.length - 1]), false);
                const cartographic = Cesium.Cartographic.fromCartesian(positions[0]);
                shape = viewer.entities.add({
                    position: activeShapePoints[0],
                    name: 'Circle',
                    ellipse: {
                        semiMinorAxis: callbackRadius,
                        semiMajorAxis: callbackRadius,
                        height: cartographic.height,
                        material: Cesium.Color.WHITE.withAlpha(0.5),
                        outline: true,
                        outlineColor: Cesium.Color.fromBytes(51, 153, 204),
                        outlineWidth: 1
                    }
                });
                Object.assign(shape, { positionData: [positions[0]], radius });
                break;
            }
            case 'Rectangle': {
                const positions = positionData instanceof Cesium.CallbackProperty
                    ? positionData.getValue(Cesium.JulianDate.now())
                    : positionData;
                const cartographic = Cesium.Cartographic.fromCartesian(positions[0]);
                shape = viewer.entities.add({
                    name: 'Rectangle',
                    rectangle: {
                        coordinates: new Cesium.CallbackProperty(() => Cesium.Rectangle.fromCartesianArray(positions), false),
                        height: cartographic.height,
                        material: Cesium.Color.WHITE.withAlpha(0.5),
                        outline: true,
                        outlineColor: Cesium.Color.fromBytes(51, 153, 204),
                        outlineWidth: 1
                    }
                });
                shape.positionData = positions;
                break;
            }
        }
        if (final)
            lastFeature = shape;
        return shape;
    }
    return drawHandler;
};