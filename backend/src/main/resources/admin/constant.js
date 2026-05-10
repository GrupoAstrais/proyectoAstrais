const API_URL = window.location.origin; //"http://172.22.238.162:5684";
const STORAGE_ACCESS = 'astrais_access';
const STORAGE_REFRESH = 'astrais_refresh';

var url_login = "/admin/login.html";
var url_index = "/admin/index.html";

if (window.location.port == "5500"){
    url_login = "/login.html";
    url_index = "/index.html";
}

const LOGIN_URL = url_login;
const INDEX_URL = url_index;

function calcularNV(lf){
    return lf / 100;
}
function calcularXPFromNV(nv){
    let mxnv = 0;
    for (let i = 0; i < nv; i++) {
        mxnv += (i+1) * 100;
    }
    return mxnv;
}
// Simplificacion de la formula del XP de forma inversa
// AF(x) = sum(0, X)((i+1)*100)
// BF(x) = (-1 + sqrt(1 + XP + 12.5)) / 2
function calcularNVFromXP(xp) {
    return Math.floor(
        (-1 + Math.sqrt(1 + xp / 12.5)) / 2
    );
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
    if (!accessToken) {
        window.location.href = LOGIN_URL;
        throw new Error("Error! Access token is not set")
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