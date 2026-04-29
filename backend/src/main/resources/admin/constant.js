const API_URL = "http://localhost:5684";
const STORAGE_ACCESS = 'astrais_access';
const STORAGE_REFRESH = 'astrais_refresh';

if (window.location.port == "5501"){
    const LOGIN_URL = "/login.html";
    const INDEX_URL = "/index.html";
}else{
    const LOGIN_URL = "/admin/login.html";
    const INDEX_URL = "/admin/index.html";
}


function checkIfNeedsRefresh(num, jsondata) {
    return num == 401 && jsondata.errorCode == 0 && jsondata.errorText == "Invalid/expired token";
}

async function refreshToken() {
    const refreshToken = localStorage.getItem(STORAGE_REFRESH);
    try {
        const res = await fetch(API_URL + '/auth/regenAccess', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json', 
                'Authorization': `Bearer ${refreshToken}`
            }
        });

        if (res.ok) {
            const data = await res.json();
            localStorage.setItem(STORAGE_ACCESS, data.newAccessToken);
            return true;
        } else {
            alert("Couldn't refresh! Back to login you go")
            return false;
        }
    } catch (e) {
        alert("Error desconocido: " + e + "!");
        return false;
    }
}


async function requestServer(url, options = {}, retryIfRefresh = true) {
    const res = await fetch(url, options);
    if (retryIfRefresh && !res.ok) {
        if (checkIfNeedsRefresh(res.status, (await res.json()))) {
            await refreshToken();
            return requestServer(url, options, false);
        }
    }

    return res;
}

async function requestServerProtected(url, options = {}, retryIfRefresh = true) {
    const accessToken = localStorage.getItem(STORAGE_ACCESS);
    if (accessToken == null) {
        throw EvalError("Error! Access token is not set")
    }

    const res = await fetch(url, {
        ...options,
        headers: {
            ...(options.headers || {}),
            'Authorization': `Bearer ${accessToken}`
        }
    });
    if (retryIfRefresh && !res.ok) {
        if (checkIfNeedsRefresh(res.status, (await res.json()))) {
            await refreshToken();
            return requestServerProtected(url, options, false);
        }
    }
    return res;
}