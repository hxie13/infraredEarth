/**
 * Shared login state management for sub-pages.
 * Checks session on page load, provides login/register modals,
 * and updates the nav bar to show logged-in user info.
 *
 * Requires: axios + csrf-axios.js loaded before this script.
 * Expects: .nav-actions container with .btn-login button in the page.
 */
(function () {
  'use strict';

  var AES_KEY = 'SITP0123456789AB';
  var BASE = '/infrared';

  // ── Expose current user globally ────────────────────────────────────
  window.currentUser = null;

  // ── AES encrypt (same as index.html) ────────────────────────────────
  async function aesEncrypt(text, key) {
    var encoder = new TextEncoder();
    var data = encoder.encode(text);
    var keyData = encoder.encode(key);
    var cryptoKey = await crypto.subtle.importKey(
      'raw', keyData, { name: 'AES-CBC' }, false, ['encrypt']
    );
    var iv = encoder.encode('A-16-Byte-String');
    var encrypted = await crypto.subtle.encrypt(
      { name: 'AES-CBC', iv: iv }, cryptoKey, data
    );
    return btoa(String.fromCharCode.apply(null, new Uint8Array(encrypted)));
  }

  // ── Inject modal CSS + HTML ─────────────────────────────────────────
  function injectLoginUI() {
    var style = document.createElement('style');
    style.textContent = '\
      .ss-modal-overlay { position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.6); z-index:20000; display:flex; align-items:center; justify-content:center; } \
      .ss-modal-overlay.hidden { display:none; } \
      .ss-modal { background:#1a1a2e; border-radius:12px; padding:32px; width:380px; max-width:90vw; color:#e0e0e0; box-shadow:0 8px 40px rgba(0,0,0,0.5); } \
      .ss-modal h3 { margin:0 0 20px; font-size:20px; text-align:center; color:#fff; } \
      .ss-modal input { width:100%; padding:10px 12px; margin-bottom:12px; border:1px solid #333; border-radius:8px; background:#0d0d1a; color:#e0e0e0; font-size:14px; box-sizing:border-box; } \
      .ss-modal input:focus { outline:none; border-color:#667eea; } \
      .ss-modal .ss-btn-primary { width:100%; padding:10px; border:none; border-radius:8px; background:linear-gradient(135deg,#667eea,#764ba2); color:#fff; font-size:15px; cursor:pointer; margin-top:4px; } \
      .ss-modal .ss-btn-primary:hover { opacity:0.9; } \
      .ss-modal .ss-error { color:#ff6b6b; font-size:13px; min-height:18px; margin-bottom:8px; } \
      .ss-modal .ss-success { color:#51cf66; font-size:13px; min-height:18px; margin-bottom:8px; } \
      .ss-modal .ss-switch { text-align:center; margin-top:16px; font-size:13px; color:#999; } \
      .ss-modal .ss-switch a { color:#667eea; cursor:pointer; text-decoration:none; } \
      .ss-modal .ss-close { position:absolute; top:12px; right:16px; background:none; border:none; color:#999; font-size:20px; cursor:pointer; } \
      .ss-user-info { display:flex; align-items:center; gap:8px; } \
      .ss-user-avatar { width:32px; height:32px; border-radius:50%; background:linear-gradient(135deg,#667eea,#764ba2); display:flex; align-items:center; justify-content:center; color:#fff; font-size:14px; font-weight:600; } \
      .ss-user-name { color:#e0e0e0; font-size:14px; font-weight:500; } \
      .ss-logout-btn { padding:4px 12px; border:1px solid #555; border-radius:6px; background:transparent; color:#ccc; font-size:12px; cursor:pointer; margin-left:4px; } \
      .ss-logout-btn:hover { border-color:#ff6b6b; color:#ff6b6b; } \
    ';
    document.head.appendChild(style);

    // Login modal
    var loginOverlay = document.createElement('div');
    loginOverlay.id = 'ssLoginOverlay';
    loginOverlay.className = 'ss-modal-overlay hidden';
    loginOverlay.innerHTML = '\
      <div class="ss-modal" style="position:relative;">\
        <button class="ss-close" onclick="window._ssCloseLogin()">&times;</button>\
        <h3>用户登录</h3>\
        <form id="ssLoginForm">\
          <input type="text" id="ssLoginUser" placeholder="用户名" required />\
          <input type="password" id="ssLoginPass" placeholder="密码" required />\
          <div class="ss-error" id="ssLoginError"></div>\
          <button type="submit" class="ss-btn-primary">登 录</button>\
        </form>\
        <div class="ss-switch">还没有账号？<a onclick="window._ssShowRegister()">立即注册</a></div>\
      </div>';
    document.body.appendChild(loginOverlay);

    // Register modal
    var regOverlay = document.createElement('div');
    regOverlay.id = 'ssRegisterOverlay';
    regOverlay.className = 'ss-modal-overlay hidden';
    regOverlay.innerHTML = '\
      <div class="ss-modal" style="position:relative;">\
        <button class="ss-close" onclick="window._ssCloseRegister()">&times;</button>\
        <h3>用户注册</h3>\
        <form id="ssRegisterForm">\
          <input type="text" id="ssRegUser" placeholder="用户名" required />\
          <input type="text" id="ssRegDisplay" placeholder="显示名称（可选）" />\
          <input type="email" id="ssRegEmail" placeholder="邮箱（可选）" />\
          <input type="password" id="ssRegPass" placeholder="密码（至少8位，含大小写、数字、特殊字符）" required />\
          <input type="password" id="ssRegPassConfirm" placeholder="确认密码" required />\
          <div class="ss-error" id="ssRegError"></div>\
          <div class="ss-success" id="ssRegSuccess"></div>\
          <button type="submit" class="ss-btn-primary">注 册</button>\
        </form>\
        <div class="ss-switch">已有账号？<a onclick="window._ssShowLogin()">返回登录</a></div>\
      </div>';
    document.body.appendChild(regOverlay);

    // Event handlers
    window._ssCloseLogin = function () { loginOverlay.classList.add('hidden'); };
    window._ssCloseRegister = function () { regOverlay.classList.add('hidden'); };
    window._ssShowRegister = function () { loginOverlay.classList.add('hidden'); regOverlay.classList.remove('hidden'); };
    window._ssShowLogin = function () { regOverlay.classList.add('hidden'); loginOverlay.classList.remove('hidden'); };

    window._ssOpenLogin = function () {
      document.getElementById('ssLoginError').textContent = '';
      document.getElementById('ssLoginUser').value = '';
      document.getElementById('ssLoginPass').value = '';
      loginOverlay.classList.remove('hidden');
    };

    window._ssOpenRegister = function () {
      document.getElementById('ssRegError').textContent = '';
      regOverlay.classList.remove('hidden');
    };

    // Login form submit
    document.getElementById('ssLoginForm').addEventListener('submit', async function (e) {
      e.preventDefault();
      var username = document.getElementById('ssLoginUser').value.trim();
      var password = document.getElementById('ssLoginPass').value;
      var errEl = document.getElementById('ssLoginError');
      errEl.textContent = '';

      if (!username || !password) { errEl.textContent = '请输入用户名和密码'; return; }

      try {
        var encUser = await aesEncrypt(username, AES_KEY);
        var encPass = await aesEncrypt(password, AES_KEY);

        var response = await fetch(BASE + '/rest/account/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username: encUser, password: encPass })
        });
        var data = await response.json();
        if (data.status === 'Success' && data.account) {
          window._ssCloseLogin();
          showLoggedIn(data.account);
        } else {
          errEl.textContent = data.errorCode || '登录失败，请检查用户名和密码';
        }
      } catch (err) {
        errEl.textContent = '登录请求失败，请稍后重试';
      }
    });

    // Register form submit
    document.getElementById('ssRegisterForm').addEventListener('submit', async function (e) {
      e.preventDefault();
      var username = document.getElementById('ssRegUser').value.trim();
      var displayname = document.getElementById('ssRegDisplay').value.trim();
      var email = document.getElementById('ssRegEmail').value.trim();
      var password = document.getElementById('ssRegPass').value;
      var confirmPass = document.getElementById('ssRegPassConfirm').value;
      var errEl = document.getElementById('ssRegError');
      var sucEl = document.getElementById('ssRegSuccess');
      errEl.textContent = '';
      sucEl.textContent = '';

      if (!username || !password) { errEl.textContent = '请输入用户名和密码'; return; }
      if (password !== confirmPass) { errEl.textContent = '两次密码不一致'; return; }
      if (password.length < 8) { errEl.textContent = '密码长度至少8位'; return; }

      try {
        var encUser = await aesEncrypt(username, AES_KEY);
        var encPass = await aesEncrypt(password, AES_KEY);
        var encDisplay = displayname ? await aesEncrypt(displayname, AES_KEY) : '';
        var encEmail = email ? await aesEncrypt(email, AES_KEY) : '';

        var response = await fetch(BASE + '/rest/account/register', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username: encUser, password: encPass, displayname: encDisplay, email: encEmail })
        });
        var data = await response.json();
        if (data.status === 'Success' && data.account) {
          sucEl.textContent = '注册成功！';
          setTimeout(function () {
            window._ssCloseRegister();
            showLoggedIn(data.account);
          }, 1000);
        } else {
          errEl.textContent = data.errorCode || '注册失败';
        }
      } catch (err) {
        errEl.textContent = '注册请求失败，请稍后重试';
      }
    });

    // Click overlay to close
    loginOverlay.addEventListener('click', function (e) { if (e.target === loginOverlay) window._ssCloseLogin(); });
    regOverlay.addEventListener('click', function (e) { if (e.target === regOverlay) window._ssCloseRegister(); });
  }

  // ── Update nav bar ──────────────────────────────────────────────────
  function showLoggedIn(account) {
    window.currentUser = account;
    var navActions = document.querySelector('.nav-actions');
    if (!navActions) return;

    var displayName = account.displayname || account.username;
    var initial = displayName.charAt(0).toUpperCase();

    navActions.innerHTML = '\
      <div class="ss-user-info">\
        <div class="ss-user-avatar">' + initial + '</div>\
        <span class="ss-user-name">' + displayName + '</span>\
        <button class="ghost-btn ss-logout-btn" onclick="window._ssLogout()">退出</button>\
      </div>';
  }

  function showGuest() {
    window.currentUser = null;
    var navActions = document.querySelector('.nav-actions');
    if (!navActions) return;

    navActions.innerHTML = '\
      <button class="ghost-btn" onclick="window._ssOpenLogin()">登录</button>\
      <button class="accent-btn" onclick="window._ssOpenRegister()">注册</button>';
  }

  // ── Logout ──────────────────────────────────────────────────────────
  window._ssLogout = async function () {
    try {
      await fetch(BASE + '/rest/account/logout');
    } catch (e) { /* ignore */ }
    showGuest();
  };

  // ── Check session on page load ──────────────────────────────────────
  async function checkSession() {
    try {
      var response = await fetch(BASE + '/rest/account/login');
      var data = await response.json();
      if (data.status === 'Success' && data.account) {
        showLoggedIn(data.account);
      } else {
        showGuest();
      }
    } catch (e) {
      showGuest();
    }
  }

  // ── Initialize ──────────────────────────────────────────────────────
  function _ssInit() {
    injectLoginUI();
    checkSession();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', _ssInit);
  } else {
    _ssInit();
  }
})();
