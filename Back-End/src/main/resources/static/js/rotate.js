class EarthRotator {
    constructor(viewer) {
        this.viewer = viewer;
        this.rotateActive = false;
        this.delta = 0.003;
    }

    start() {
        this.rotateActive = true;
        this.viewer.scene.postUpdate.addEventListener(this.rotate);
    }

    stop() {
        this.rotateActive = false;
        this.viewer.scene.postUpdate.removeEventListener(this.rotate);
    }

    rotate = () => {
        if (this.rotateActive) {
            this.viewer.scene.camera.rotate(Cesium.Cartesian3.UNIT_Z, -this.delta);
        }
    };
}
