(function () {
  const CESIUM_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiIyZjU4YmI5NS0xOTkxLTQyMWItOWE2ZS1hZGNhYTU1YzRlNTQiLCJpZCI6MzE4MTAyLCJpYXQiOjE3NTE1MjkwMDR9.8WjfY2pPzxw6kVxpSk6a0PHycFOUUcb4jyNh9j8lpW4";
  const TDT_KEY = "a090fdbe1ebf350ab3eba3adef7d3417";
  const CONTACT_EMAIL = "infraredearth@mail.sitp.ac.cn";
  const NAV_LINK_SELECTOR = ".js-nav-link[href^='#']";

  function qs(selector) {
    return document.querySelector(selector);
  }

  function qsa(selector) {
    return Array.from(document.querySelectorAll(selector));
  }

  function encodeMailtoValue(value) {
    return encodeURIComponent(value || "");
  }

  function activateNav(hash) {
    qsa(NAV_LINK_SELECTOR).forEach((link) => {
      link.classList.toggle("is-active", link.getAttribute("href") === hash);
    });
  }

  function initMobileMenu() {
    const toggle = qs("#mobileMenuToggle");
    const menu = qs("#mobileMenu");
    if (!toggle || !menu) {
      return;
    }

    function closeMenu() {
      toggle.setAttribute("aria-expanded", "false");
      menu.hidden = true;
    }

    function openMenu() {
      toggle.setAttribute("aria-expanded", "true");
      menu.hidden = false;
    }

    toggle.addEventListener("click", function () {
      if (menu.hidden) {
        openMenu();
      } else {
        closeMenu();
      }
    });

    qsa("#mobileMenu a").forEach((link) => {
      link.addEventListener("click", closeMenu);
    });

    window.addEventListener("resize", function () {
      if (window.innerWidth > 960) {
        closeMenu();
      }
    });
  }

  function initActiveSections() {
    const sections = ["#home", "#modules", "#about-platform", "#workflow", "#scenarios", "#contact"]
      .map((hash) => ({ hash: hash, element: qs(hash) }))
      .filter((item) => item.element);

    if (sections.length === 0) {
      return;
    }

    activateNav("#home");

    const observer = new IntersectionObserver(
      function (entries) {
        const visibleEntry = entries
          .filter((entry) => entry.isIntersecting)
          .sort((a, b) => b.intersectionRatio - a.intersectionRatio)[0];

        if (visibleEntry) {
          activateNav("#" + visibleEntry.target.id);
        }
      },
      {
        rootMargin: "-25% 0px -55% 0px",
        threshold: [0.2, 0.45, 0.7]
      }
    );

    sections.forEach((item) => observer.observe(item.element));
  }

  function initVideoPlayback() {
    const video = qs("#platformVideo");
    const status = qs("#videoStatus");
    if (!video) {
      return;
    }

    video.muted = true;
    const playPromise = video.play();
    if (playPromise && typeof playPromise.catch === "function") {
      playPromise.catch(function () {
        if (status) {
          status.textContent = "当前浏览器限制自动播放，已回退为静态封面展示。";
        }
      });
    }
  }

  function initContactForm() {
    const form = qs("#contactForm");
    const status = qs("#contactStatus");
    if (!form) {
      return;
    }

    form.addEventListener("submit", function (event) {
      event.preventDefault();
      const formData = new FormData(form);
      const name = String(formData.get("name") || "").trim();
      const email = String(formData.get("email") || "").trim();
      const subject = String(formData.get("subject") || "").trim();
      const message = String(formData.get("message") || "").trim();

      if (!name && !email && !subject && !message) {
        if (status) {
          status.textContent = "请至少填写一项联系信息或留言内容。";
        }
        return;
      }

      const finalSubject = subject || "红外地球平台咨询";
      const body = [
        "姓名: " + (name || "未填写"),
        "邮箱: " + (email || "未填写"),
        "",
        "留言内容:",
        message || "未填写"
      ].join("\n");

      if (status) {
        status.textContent = "正在生成邮件草稿...";
      }

      window.location.href =
        "mailto:" + CONTACT_EMAIL +
        "?subject=" + encodeMailtoValue(finalSubject) +
        "&body=" + encodeMailtoValue(body);
    });
  }

  function initSubscribeForm() {
    const form = qs("#subscribeForm");
    const status = qs("#subscribeStatus");
    if (!form) {
      return;
    }

    form.addEventListener("submit", function (event) {
      event.preventDefault();
      const formData = new FormData(form);
      const email = String(formData.get("email") || "").trim();

      if (!email) {
        if (status) {
          status.textContent = "请输入订阅邮箱地址。";
        }
        return;
      }

      if (status) {
        status.textContent = "正在生成订阅邮件草稿...";
      }

      window.location.href =
        "mailto:" + CONTACT_EMAIL +
        "?subject=" + encodeMailtoValue("红外地球平台订阅更新") +
        "&body=" + encodeMailtoValue("请将以下邮箱加入平台更新订阅名单:\n" + email);
    });
  }

  function calculateScaleDenominator(viewer) {
    const centerX = viewer.canvas.clientWidth / 2;
    const centerY = viewer.canvas.clientHeight / 2;
    const cartesian2 = new Cesium.Cartesian2(centerX, centerY);
    const ray = viewer.camera.getPickRay(cartesian2);
    const cartesian = viewer.scene.globe.pick(ray, viewer.scene);

    if (!cartesian) {
      return { scale: "-", resolution: "-" };
    }

    const boundingSphere = new Cesium.BoundingSphere();
    boundingSphere.center = cartesian;
    boundingSphere.radius = 0.1;
    const resolution = viewer.scene.camera.getPixelSize(
      boundingSphere,
      viewer.scene.context.drawingBufferWidth,
      viewer.scene.context.drawingBufferHeight
    );

    return {
      scale: (96 * resolution) / 0.0254,
      resolution: resolution
    };
  }

  function getZoomLevel(viewer) {
    const height = viewer.camera.positionCartographic.height;
    return Math.ceil(21 - Math.log(height / 1000) / Math.LN2) - 4;
  }

  function initCesium() {
    if (typeof window.Cesium === "undefined") {
      return;
    }

    const container = qs("#cesiumContainer");
    if (!container) {
      return;
    }

    Cesium.Ion.defaultAccessToken = CESIUM_TOKEN;

    const viewer = new Cesium.Viewer("cesiumContainer", {
      geocoder: false,
      homeButton: false,
      baseLayerPicker: false,
      timeline: false,
      animation: false,
      navigationHelpButton: false,
      infoBox: false,
      selectionIndicator: false,
      fullscreenButton: false,
      sceneModePicker: false
    });

    const basicLayer = new Cesium.ImageryLayer(
      new Cesium.WebMapTileServiceImageryProvider({
        url: "http://t0.tianditu.gov.cn/img_w/wmts?service=wmts&request=GetTile&version=1.0.0&LAYER=img&tileMatrixSet=w&TileMatrix={TileMatrix}&TileRow={TileRow}&TileCol={TileCol}&style=default&format=tiles&tk=" + TDT_KEY,
        layer: "tdtBasicLayer",
        style: "default",
        format: "image/jpeg",
        tileMatrixSetID: "GoogleMapsCompatible"
      })
    );

    const labelLayer = new Cesium.ImageryLayer(
      new Cesium.WebMapTileServiceImageryProvider({
        url: "http://t0.tianditu.gov.cn/cia_w/wmts?service=wmts&request=GetTile&version=1.0.0&LAYER=cia&tileMatrixSet=w&TileMatrix={TileMatrix}&TileRow={TileRow}&TileCol={TileCol}&style=default&format=tiles&tk=" + TDT_KEY,
        layer: "tdtAnnoLayer",
        style: "default",
        format: "image/jpeg",
        tileMatrixSetID: "GoogleMapsCompatible"
      })
    );

    viewer.imageryLayers.add(basicLayer);
    viewer.imageryLayers.add(labelLayer);
    viewer.cesiumWidget.creditContainer.style.display = "none";
    viewer.scene.globe.showGroundAtmosphere = true;
    viewer.scene.skyAtmosphere.show = true;
    viewer.scene.fxaa = true;
    viewer.scene.postProcessStages.fxaa.enabled = true;
    viewer.scene.globe.depthTestAgainstTerrain = false;
    viewer.scene.screenSpaceCameraController.minimumZoomDistance = 900000;
    viewer.scene.screenSpaceCameraController.maximumZoomDistance = 24000000;

    function flyToGlobal() {
      viewer.camera.flyTo({
        destination: Cesium.Cartesian3.fromDegrees(88, 27, 18000000),
        duration: 1.8
      });
    }

    function flyToAsia() {
      viewer.camera.flyTo({
        destination: Cesium.Cartesian3.fromDegrees(103.84, 31.15, 9500000),
        duration: 1.6
      });
    }

    class EarthRotator {
      constructor(targetViewer) {
        this.viewer = targetViewer;
        this.delta = 0.0008;
        this.rotate = this.rotate.bind(this);
      }

      start() {
        this.viewer.scene.postUpdate.removeEventListener(this.rotate);
        this.viewer.scene.postUpdate.addEventListener(this.rotate);
      }

      stop() {
        this.viewer.scene.postUpdate.removeEventListener(this.rotate);
      }

      rotate() {
        this.viewer.scene.camera.rotate(Cesium.Cartesian3.UNIT_Z, -this.delta);
      }
    }

    const rotator = new EarthRotator(viewer);
    flyToAsia();
    rotator.start();

    viewer.screenSpaceEventHandler.setInputAction(function () {
      rotator.stop();
    }, Cesium.ScreenSpaceEventType.LEFT_DOWN);

    const mouseHandler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas);
    mouseHandler.setInputAction(function (movement) {
      const cartesian = viewer.scene.globe.pick(
        viewer.camera.getPickRay(movement.endPosition),
        viewer.scene
      );

      if (!cartesian) {
        return;
      }

      const cartographic = Cesium.Cartographic.fromCartesian(cartesian);
      const lon = Cesium.Math.toDegrees(cartographic.longitude).toFixed(2);
      const lat = Cesium.Math.toDegrees(cartographic.latitude).toFixed(2);
      const coordinate = qs("#coordinate");
      if (coordinate) {
        coordinate.textContent = lon + ", " + lat;
      }
    }, Cesium.ScreenSpaceEventType.MOUSE_MOVE);

    viewer.scene.postRender.addEventListener(function () {
      const zoomLevel = qs("#zoomLevel");
      const mapScale = qs("#mapScale");
      if (zoomLevel) {
        zoomLevel.textContent = String(getZoomLevel(viewer));
      }
      if (mapScale) {
        const scale = calculateScaleDenominator(viewer);
        mapScale.textContent = scale.scale === "-" ? "-" : String(Math.round(scale.scale));
      }
    });

    function setToolbarActive(activeId) {
      ["focusGlobal", "focusAsia", "resumeRotation"].forEach(function (id) {
        const button = qs("#" + id);
        if (button) {
          button.classList.toggle("is-active", id === activeId);
        }
      });
    }

    const focusGlobalButton = qs("#focusGlobal");
    const focusAsiaButton = qs("#focusAsia");
    const resumeRotationButton = qs("#resumeRotation");

    if (focusGlobalButton) {
      focusGlobalButton.addEventListener("click", function () {
        setToolbarActive("focusGlobal");
        flyToGlobal();
      });
    }

    if (focusAsiaButton) {
      focusAsiaButton.addEventListener("click", function () {
        setToolbarActive("focusAsia");
        flyToAsia();
      });
    }

    if (resumeRotationButton) {
      resumeRotationButton.addEventListener("click", function () {
        setToolbarActive("resumeRotation");
        rotator.start();
      });
    }
  }

  function initPortal() {
    initMobileMenu();
    initActiveSections();
    initVideoPlayback();
    initContactForm();
    initSubscribeForm();
    initCesium();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initPortal);
  } else {
    initPortal();
  }
})();
