/**
 * Shared axios instance with CSRF token support.
 * Reads XSRF-TOKEN cookie and sends it as X-CSRF-Token header on POST/PUT/DELETE requests.
 */
(function () {
    function getCookie(name) {
        const match = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'));
        return match ? decodeURIComponent(match[1]) : null;
    }

    const apiClient = axios.create({
        baseURL: window.location.origin
    });

    apiClient.interceptors.request.use(function (config) {
        var method = (config.method || 'get').toUpperCase();
        if (method !== 'GET' && method !== 'HEAD' && method !== 'OPTIONS') {
            var token = getCookie('XSRF-TOKEN');
            if (token) {
                config.headers['X-CSRF-Token'] = token;
            }
        }
        return config;
    });

    // Expose globally
    window.apiClient = apiClient;
})();
