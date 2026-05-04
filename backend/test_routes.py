#!/usr/bin/env python3
"""
Script para probar todos los endpoints del backend Ktor de Astrais.

Uso CLI:
    python test_routes.py [--base-url http://localhost:8080]

Uso Web GUI:
    python test_routes.py --gui

Requiere:
    pip install requests
"""

import argparse
import json
import sys
import threading
import time
from http.server import HTTPServer, BaseHTTPRequestHandler
from typing import Optional
from urllib.parse import urlparse
import webbrowser

import requests
from requests import Response

GREEN = "\033[92m"
RED = "\033[91m"
YELLOW = "\033[93m"
BLUE = "\033[94m"
CYAN = "\033[96m"
BOLD = "\033[1m"
RESET = "\033[0m"

GUI_HTML = """<!DOCTYPE html>
<html lang="es" class="dark">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Astrais - API Route Tester</title>
<script src="https://cdn.tailwindcss.com"></script>
<script>
  tailwind.config = {
    darkMode: 'class',
    theme: { extend: { fontFamily: { mono: ['JetBrains Mono', 'Fira Code', 'monospace'] } } }
  }
</script>
<style>
  ::-webkit-scrollbar { width: 6px; height: 6px; }
  ::-webkit-scrollbar-track { background: #1e293b; }
  ::-webkit-scrollbar-thumb { background: #475569; border-radius: 3px; }
  ::-webkit-scrollbar-thumb:hover { background: #64748b; }
</style>
</head>
<body class="bg-gray-950 text-gray-100 min-h-screen font-sans">
<div class="max-w-[1400px] mx-auto p-5">

  <header class="flex items-center justify-between flex-wrap gap-3 mb-5 pb-4 border-b border-gray-800">
    <h1 class="text-xl font-bold text-sky-400">Astrais API Tester <span class="text-sm text-gray-500 font-normal">v2.0</span></h1>
    <div class="flex items-center gap-2">
      <input id="urlInput" value="http://localhost:8080" placeholder="http://localhost:8080"
        class="px-3 py-2 border border-gray-700 rounded-lg bg-gray-900 text-gray-100 font-mono text-sm w-72 focus:outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500">
      <button id="runBtn" onclick="runAll()"
        class="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-sm rounded-lg transition disabled:opacity-40 disabled:cursor-not-allowed">Ejecutar</button>
      <button id="stopBtn" onclick="stopTests()" disabled
        class="px-4 py-2 bg-red-600 hover:bg-red-700 text-white font-semibold text-sm rounded-lg transition disabled:opacity-40 disabled:cursor-not-allowed">Parar</button>
      <button onclick="exportJSON()"
        class="px-4 py-2 bg-gray-800 hover:bg-gray-700 text-gray-100 font-semibold text-sm rounded-lg border border-gray-700 transition">JSON</button>
      <button onclick="clearAll()"
        class="px-4 py-2 bg-gray-800 hover:bg-gray-700 text-gray-100 font-semibold text-sm rounded-lg border border-gray-700 transition">Limpiar</button>
    </div>
  </header>

  <div class="flex gap-2 flex-wrap mb-4" id="phaseBtns">
    <button class="px-3 py-1 bg-gray-800 text-gray-400 border border-gray-700 rounded text-xs font-semibold hover:text-sky-400 hover:border-sky-500 transition btn-phase" onclick="runPhase(0)">1. Publicas</button>
    <button class="px-3 py-1 bg-gray-800 text-gray-400 border border-gray-700 rounded text-xs font-semibold hover:text-sky-400 hover:border-sky-500 transition btn-phase" onclick="runPhase(1)">2. Auth</button>
    <button class="px-3 py-1 bg-gray-800 text-gray-400 border border-gray-700 rounded text-xs font-semibold hover:text-sky-400 hover:border-sky-500 transition btn-phase" onclick="runPhase(2)">3. Rutas Auth</button>
    <button class="px-3 py-1 bg-gray-800 text-gray-400 border border-gray-700 rounded text-xs font-semibold hover:text-sky-400 hover:border-sky-500 transition btn-phase" onclick="runPhase(3)">4. OAuth</button>
    <button class="px-3 py-1 bg-gray-800 text-gray-400 border border-gray-700 rounded text-xs font-semibold hover:text-sky-400 hover:border-sky-500 transition btn-phase" onclick="runPhase(4)">5. Avatar</button>
    <button class="px-3 py-1 bg-gray-800 text-gray-400 border border-gray-700 rounded text-xs font-semibold hover:text-sky-400 hover:border-sky-500 transition btn-phase" onclick="runPhase(5)">6. Grupos</button>
    <button class="px-3 py-1 bg-gray-800 text-gray-400 border border-gray-700 rounded text-xs font-semibold hover:text-sky-400 hover:border-sky-500 transition btn-phase" onclick="runPhase(6)">7. Tareas</button>
    <button class="px-3 py-1 bg-gray-800 text-gray-400 border border-gray-700 rounded text-xs font-semibold hover:text-sky-400 hover:border-sky-500 transition btn-phase" onclick="runPhase(7)">8. Tienda</button>
    <button class="px-3 py-1 bg-gray-800 text-gray-400 border border-gray-700 rounded text-xs font-semibold hover:text-sky-400 hover:border-sky-500 transition btn-phase" onclick="runPhase(8)">9. Admin</button>
    <button class="px-3 py-1 bg-gray-800 text-gray-400 border border-gray-700 rounded text-xs font-semibold hover:text-sky-400 hover:border-sky-500 transition btn-phase" onclick="runPhase(9)">10. Estaticas</button>
  </div>

  <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-4">
    <div class="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">
      <div class="px-4 py-2 bg-gray-800 text-xs font-semibold uppercase tracking-wider text-gray-500 flex justify-between">
        <span>Log de ejecucion</span>
        <span id="logCount">0 lineas</span>
      </div>
      <div class="h-80 overflow-y-auto p-3 font-mono text-xs leading-relaxed" id="logPanel"></div>
    </div>
    <div class="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">
      <div class="px-4 py-2 bg-gray-800 text-xs font-semibold uppercase tracking-wider text-gray-500">Detalle de ruta</div>
      <div class="p-4 max-h-80 overflow-y-auto" id="detailPanel">
        <div class="text-gray-500 text-center py-10 italic">Haz clic en una fila de la tabla para ver los detalles de la respuesta</div>
      </div>
    </div>
  </div>

  <div class="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden mb-4">
    <div class="px-4 py-2 bg-gray-800 text-xs font-semibold uppercase tracking-wider text-gray-500 flex items-center justify-between">
      <span>Resultados</span>
      <div class="flex gap-2">
        <button class="px-3 py-0.5 border border-gray-700 rounded text-xs font-semibold text-gray-400 hover:text-gray-100 hover:border-gray-500 transition filter-btn active" onclick="filterTable('all', this)">Todos</button>
        <button class="px-3 py-0.5 border border-gray-700 rounded text-xs font-semibold text-gray-400 hover:text-gray-100 hover:border-gray-500 transition filter-btn" onclick="filterTable('pass', this)">Pass</button>
        <button class="px-3 py-0.5 border border-gray-700 rounded text-xs font-semibold text-gray-400 hover:text-gray-100 hover:border-gray-500 transition filter-btn" onclick="filterTable('fail', this)">Fail</button>
      </div>
    </div>
    <div class="max-h-96 overflow-y-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="bg-gray-800 text-gray-500 text-xs uppercase tracking-wide">
            <th class="px-4 py-2 text-left font-semibold sticky top-0 z-10">Metodo</th>
            <th class="px-4 py-2 text-left font-semibold sticky top-0 z-10">Path</th>
            <th class="px-4 py-2 text-left font-semibold sticky top-0 z-10">Descripcion</th>
            <th class="px-4 py-2 text-left font-semibold sticky top-0 z-10">Status</th>
            <th class="px-4 py-2 text-left font-semibold sticky top-0 z-10">Resultado</th>
          </tr>
        </thead>
        <tbody id="resultsBody"></tbody>
      </table>
    </div>
  </div>

  <div class="flex items-center gap-4 p-3 bg-gray-900 border border-gray-800 rounded-xl">
    <div class="flex-1 h-1.5 bg-gray-800 rounded overflow-hidden">
      <div class="h-full w-0 bg-gradient-to-r from-sky-500 to-emerald-500 transition-all duration-300 rounded" id="progressBar"></div>
    </div>
    <span class="text-sm text-gray-400 min-w-[180px]" id="statusText">Listo para ejecutar</span>
  </div>

  <div class="flex gap-8 mt-3">
    <div class="text-center"><div class="text-2xl font-bold text-gray-400" id="statTotal">0</div><div class="text-xs text-gray-600 uppercase tracking-wider">Total</div></div>
    <div class="text-center"><div class="text-2xl font-bold text-emerald-400" id="statPass">0</div><div class="text-xs text-gray-600 uppercase tracking-wider">Pass</div></div>
    <div class="text-center"><div class="text-2xl font-bold text-red-400" id="statFail">0</div><div class="text-xs text-gray-600 uppercase tracking-wider">Fail</div></div>
  </div>
</div>

<script>
const BASE = window.location.origin;
let running = false, stopFlag = false, selectedIdx = -1;
let allResults = [];

function log(msg, cls) {
  cls = cls || 'info';
  var p = document.getElementById('logPanel');
  var d = document.createElement('div');
  var ts = new Date().toLocaleTimeString();
  var colors = { pass: 'text-emerald-400', fail: 'text-red-400', info: 'text-gray-400', sect: 'text-sky-400 font-bold', err: 'text-orange-400' };
  d.className = 'py-0.5';
  d.innerHTML = '<span class="text-gray-600">[' + ts + ']</span> <span class="' + (colors[cls] || 'text-gray-400') + '">' + escHtml(msg) + '</span>';
  p.appendChild(d);
  p.scrollTop = p.scrollHeight;
  document.getElementById('logCount').textContent = p.children.length + ' lineas';
}

function escHtml(s) {
  var d = document.createElement('div');
  d.textContent = s;
  return d.innerHTML;
}

async function postApi(path, body) {
  var res = await fetch(BASE + path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: body ? JSON.stringify(body) : '{}'
  });
  return await res.json();
}

function updateStats(total, passed, failed) {
  document.getElementById('statTotal').textContent = total;
  document.getElementById('statPass').textContent = passed;
  document.getElementById('statFail').textContent = failed;
}

function renderTable(results) {
  var tbody = document.getElementById('resultsBody');
  tbody.innerHTML = '';
  results.forEach(function(r, i) {
    var tr = document.createElement('tr');
    tr.setAttribute('data-idx', i);
    if (i === selectedIdx) tr.className = 'bg-sky-500/10';
    var ok = r.success;
    var mColors = { GET: 'bg-sky-500/15 text-sky-400', POST: 'bg-emerald-500/15 text-emerald-400', PATCH: 'bg-yellow-500/15 text-yellow-400', DELETE: 'bg-red-500/15 text-red-400' };
    var mCls = mColors[r.method] || 'bg-gray-700 text-gray-300';
    tr.innerHTML =
      '<td class="px-4 py-2"><span class="font-bold text-xs px-1.5 py-0.5 rounded ' + mCls + '">' + r.method + '</span></td>' +
      '<td class="px-4 py-2 font-mono text-xs text-violet-400">' + escHtml(r.path) + '</td>' +
      '<td class="px-4 py-2 text-gray-300">' + escHtml(r.description) + '</td>' +
      '<td class="px-4 py-2 text-gray-400">' + r.status + '</td>' +
      '<td class="px-4 py-2"><span class="px-2 py-0.5 rounded text-xs font-semibold ' + (ok ? 'bg-emerald-500/15 text-emerald-400' : 'bg-red-500/15 text-red-400') + '">' + (ok ? 'PASS' : 'FAIL') + '</span></td>';
    tr.style.cursor = 'pointer';
    tr.addEventListener('click', function() { showDetail(i); });
    tbody.appendChild(tr);
  });
}

function showDetail(idx) {
  selectedIdx = idx;
  var r = allResults[idx];
  if (!r) return;
  var rows = document.querySelectorAll('#resultsBody tr');
  rows.forEach(function(tr) { tr.className = ''; });
  var sel = document.querySelector('#resultsBody tr[data-idx="' + idx + '"]');
  if (sel) sel.className = 'bg-sky-500/10';

  var dp = document.getElementById('detailPanel');
  var mColors = { GET: 'bg-sky-500/15 text-sky-400', POST: 'bg-emerald-500/15 text-emerald-400', PATCH: 'bg-yellow-500/15 text-yellow-400', DELETE: 'bg-red-500/15 text-red-400' };
  var mCls = mColors[r.method] || 'bg-gray-700 text-gray-300';
  var bodyHtml = r.body ? '<pre class="bg-gray-950 p-3 rounded-lg text-xs leading-relaxed overflow-x-auto border border-gray-800 mt-2">' + escHtml(r.body) + '</pre>' : '<div class="text-gray-500 text-center py-6 italic">Sin cuerpo de respuesta</div>';
  dp.innerHTML =
    '<div class="grid grid-cols-[80px_1fr] gap-2 text-sm mb-2"><span class="text-gray-500 font-semibold">Metodo</span><span><span class="font-bold text-xs px-1.5 py-0.5 rounded ' + mCls + '">' + r.method + '</span></span></div>' +
    '<div class="grid grid-cols-[80px_1fr] gap-2 text-sm mb-2"><span class="text-gray-500 font-semibold">Path</span><span class="font-mono text-xs text-violet-400">' + escHtml(r.path) + '</span></div>' +
    '<div class="grid grid-cols-[80px_1fr] gap-2 text-sm mb-2"><span class="text-gray-500 font-semibold">Status</span><span>' + r.status + '</span></div>' +
    '<div class="grid grid-cols-[80px_1fr] gap-2 text-sm mb-2"><span class="text-gray-500 font-semibold">Resultado</span><span class="px-2 py-0.5 rounded text-xs font-semibold ' + (r.success ? 'bg-emerald-500/15 text-emerald-400' : 'bg-red-500/15 text-red-400') + '">' + (r.success ? 'PASS' : 'FAIL') + '</span></div>' +
    '<div class="grid grid-cols-[80px_1fr] gap-2 text-sm mt-3"><span class="text-gray-500 font-semibold">Body</span></div>' +
    bodyHtml;
}

function filterTable(mode, btn) {
  document.querySelectorAll('.filter-btn').forEach(function(b) { b.className = b.className.replace('active', '').replace('bg-gray-800 text-sky-400 border-sky-500', ''); });
  if (btn) btn.className += ' active bg-gray-800 text-sky-400 border-sky-500';
  var rows = document.querySelectorAll('#resultsBody tr');
  rows.forEach(function(tr) {
    var badge = tr.querySelector('span[class*="font-semibold"]');
    if (!badge) return;
    var isPass = badge.textContent === 'PASS';
    if (mode === 'all') tr.style.display = '';
    else if (mode === 'pass') tr.style.display = isPass ? '' : 'none';
    else tr.style.display = isPass ? 'none' : '';
  });
}

async function runAll() {
  if (running) return;
  running = true; stopFlag = false;
  document.getElementById('runBtn').disabled = true;
  document.getElementById('stopBtn').disabled = false;
  document.getElementById('logPanel').innerHTML = '';
  document.getElementById('resultsBody').innerHTML = '';
  document.getElementById('progressBar').style.width = '0%';
  document.getElementById('statusText').textContent = 'Iniciando...';
  allResults = [];
  selectedIdx = -1;

  try {
    await postApi('/api/set_url', { url: document.getElementById('urlInput').value });
  } catch(e) {
    log('Error conectando al proxy: ' + e.message, 'fail');
    running = false;
    document.getElementById('runBtn').disabled = false;
    document.getElementById('stopBtn').disabled = true;
    return;
  }

  var phases = [
    'test_public_routes', 'authenticate', 'test_auth_routes',
    'test_oauth_routes', 'test_avatar_routes', 'test_group_routes',
    'test_task_routes', 'test_store_routes', 'test_admin_routes', 'test_static_routes'
  ];
  var phaseLabels = ['Publicas', 'Autenticacion', 'Rutas Auth', 'OAuth', 'Avatar', 'Grupos', 'Tareas', 'Tienda', 'Admin', 'Estaticas'];

  for (var i = 0; i < phases.length; i++) {
    if (stopFlag) { log('Ejecucion detenida por el usuario', 'err'); break; }
    log('======= Fase ' + (i+1) + '/10: ' + phaseLabels[i] + ' =======', 'sect');
    var phaseBtns = document.querySelectorAll('.btn-phase');
    if (phaseBtns[i]) { phaseBtns[i].className = 'px-3 py-1 bg-sky-500/15 text-sky-400 border border-sky-500 rounded text-xs font-semibold transition btn-phase'; }

    try {
      var res = await postApi('/api/run_phase', { phase: phases[i] });
      if (res.logs && Array.isArray(res.logs)) {
        res.logs.forEach(function(l) { log(l.msg, l.cls); });
      }
      var data = await postApi('/api/results', {});
      if (data.results && Array.isArray(data.results)) {
        allResults = data.results;
        renderTable(allResults);
      }
    } catch(e) {
      log('Error en fase ' + (i+1) + ': ' + e.message, 'fail');
    }
    document.getElementById('progressBar').style.width = ((i + 1) / phases.length * 100) + '%';
    if (phaseBtns[i]) { phaseBtns[i].className = 'px-3 py-1 bg-gray-800 text-gray-400 border border-gray-700 rounded text-xs font-semibold hover:text-sky-400 hover:border-sky-500 transition btn-phase'; }
  }

  try {
    var sum = await postApi('/api/summary', {});
    updateStats(sum.total || 0, sum.passed || 0, sum.failed || 0);
    document.getElementById('statusText').textContent = 'Completado - ' + (sum.passed||0) + '/' + (sum.total||0) + ' pass';
  } catch(e) {}

  document.getElementById('progressBar').style.width = '100%';
  running = false;
  document.getElementById('runBtn').disabled = false;
  document.getElementById('stopBtn').disabled = true;
}

async function runPhase(idx) {
  if (running) return;
  var phases = [
    'test_public_routes', 'authenticate', 'test_auth_routes',
    'test_oauth_routes', 'test_avatar_routes', 'test_group_routes',
    'test_task_routes', 'test_store_routes', 'test_admin_routes', 'test_static_routes'
  ];
  var phaseLabels = ['Publicas', 'Autenticacion', 'Rutas Auth', 'OAuth', 'Avatar', 'Grupos', 'Tareas', 'Tienda', 'Admin', 'Estaticas'];
  if (idx >= phases.length) return;

  running = true;
  document.getElementById('runBtn').disabled = true;
  document.getElementById('logPanel').innerHTML = '';
  document.getElementById('resultsBody').innerHTML = '';
  document.getElementById('progressBar').style.width = '0%';
  document.getElementById('statusText').textContent = 'Fase ' + (idx+1) + ': ' + phaseLabels[idx];
  allResults = [];
  selectedIdx = -1;

  await postApi('/api/set_url', { url: document.getElementById('urlInput').value });
  log('======= Fase ' + (idx+1) + '/10: ' + phaseLabels[idx] + ' =======', 'sect');

  try {
    var res = await postApi('/api/run_phase', { phase: phases[idx] });
    if (res.logs && Array.isArray(res.logs)) {
      res.logs.forEach(function(l) { log(l.msg, l.cls); });
    }
    var data = await postApi('/api/results', {});
    if (data.results && Array.isArray(data.results)) {
      allResults = data.results;
      renderTable(allResults);
    }
  } catch(e) {
    log('Error: ' + e.message, 'fail');
  }

  var sum = await postApi('/api/summary', {});
  updateStats(sum.total || 0, sum.passed || 0, sum.failed || 0);
  document.getElementById('statusText').textContent = 'Fase ' + (idx+1) + ' completada';
  document.getElementById('progressBar').style.width = '100%';
  running = false;
  document.getElementById('runBtn').disabled = false;
}

function stopTests() {
  stopFlag = true;
  log('Deteniendo ejecucion...', 'err');
}

async function exportJSON() {
  if (!allResults.length) { alert('No hay resultados para exportar'); return; }
  var blob = new Blob([JSON.stringify(allResults, null, 2)], { type: 'application/json' });
  var a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = 'test_results.json';
  a.click();
}

function clearAll() {
  document.getElementById('logPanel').innerHTML = '';
  document.getElementById('resultsBody').innerHTML = '';
  document.getElementById('detailPanel').innerHTML = '<div class="text-gray-500 text-center py-10 italic">Haz clic en una fila de la tabla para ver los detalles de la respuesta</div>';
  document.getElementById('progressBar').style.width = '0%';
  document.getElementById('statusText').textContent = 'Listo para ejecutar';
  allResults = []; selectedIdx = -1;
  updateStats(0, 0, 0);
  postApi('/api/clear', {});
}
</script>
</body>
</html>"""


class RouteTester:
    def __init__(self, base_url: str, callback=None):
        self.base_url = base_url.rstrip("/")
        self.session = requests.Session()
        self.access_token: Optional[str] = None
        self.refresh_token: Optional[str] = None
        self.results: list[dict] = []
        self.logs: list[dict] = []
        self.test_user: Optional[dict] = None
        self.created_group_id: Optional[int] = None
        self.created_task_id: Optional[int] = None
        self.me_uid: Optional[int] = None
        self.callback = callback

    def log(self, message: str, color: str = ""):
        color_map = {"": "info", GREEN: "pass", RED: "fail", YELLOW: "err", CYAN: "info", BLUE: "sect"}
        cls = color_map.get(color, "info")
        msg_clean = message.replace("\033[92m", "").replace("\033[91m", "").replace("\033[93m", "")
        msg_clean = msg_clean.replace("\033[94m", "").replace("\033[96m", "").replace("\033[1m", "")
        msg_clean = msg_clean.replace("\033[0m", "")
        print(msg_clean)
        self.logs.append({"msg": msg_clean, "cls": cls})
        if self.callback:
            self.callback(message, color)

    def make_request(
        self,
        method: str,
        path: str,
        json_data: Optional[dict] = None,
        params: Optional[dict] = None,
        use_auth: bool = False,
        allow_redirects: bool = True,
        use_refresh_token: bool = False,
    ) -> Response:
        url = f"{self.base_url}{path}"
        headers = {}
        if use_auth:
            token = self.refresh_token if (use_refresh_token and self.refresh_token) else self.access_token
            if token:
                headers["Authorization"] = f"Bearer {token}"
        try:
            response = self.session.request(
                method=method,
                url=url,
                json=json_data,
                params=params,
                headers=headers,
                allow_redirects=allow_redirects,
                timeout=10,
            )
            return response
        except requests.exceptions.ConnectionError:
            self.log(f"  ERROR: No se pudo conectar a {url}", RED)
            raise
        except requests.exceptions.Timeout:
            self.log(f"  ERROR: Timeout conectando a {url}", RED)
            raise

    def test_route(
        self,
        method: str,
        path: str,
        description: str,
        json_data: Optional[dict] = None,
        params: Optional[dict] = None,
        use_auth: bool = False,
        expected_status: int = 200,
        allow_redirects: bool = True,
        use_refresh_token: bool = False,
        valid_statuses: Optional[list] = None,
    ) -> bool:
        try:
            response = self.make_request(method, path, json_data, params, use_auth, allow_redirects, use_refresh_token)

            if use_auth and not self.access_token:
                success = response.status_code in (401, 403)
                status_text = f"OK (sin auth: {response.status_code})" if success else f"FAIL (esperado 401/403, obtenido {response.status_code})"
            elif valid_statuses and response.status_code in valid_statuses:
                success = True
                status_text = f"{response.status_code}"
            else:
                success = response.status_code == expected_status or (
                    expected_status == 200 and response.status_code < 400
                ) or (
                    expected_status == 400 and response.status_code in (400, 401, 403, 404, 409)
                )
                status_text = f"{response.status_code}"

            body_preview = ""
            if response.text:
                try:
                    body_data = response.json()
                    body_preview = json.dumps(body_data, ensure_ascii=False)[:200]
                except (json.JSONDecodeError, ValueError):
                    body_preview = response.text[:200]

            self.results.append({
                "method": method, "path": path, "description": description,
                "status": response.status_code, "success": success,
                "body": body_preview,
            })

            symbol = "PASS" if success else "FAIL"
            extra = f" [{body_preview}]" if body_preview and not success else ""
            self.log(f"  [{symbol:4s}] {method:7s} {path:45s} -> {status_text}{extra}")

            if response.ok and response.text:
                try:
                    data = response.json()
                    if method == "POST" and path == "/auth/login" and response.ok:
                        self.access_token = data.get("jwtAccessToken") or self.access_token
                        self.refresh_token = data.get("jwtRefreshToken") or self.refresh_token
                    if method == "POST" and path == "/groups/createGroup" and response.ok:
                        self.created_group_id = data.get("groupId")
                    if method == "POST" and path == "/tasks" and response.ok:
                        self.created_task_id = data.get("id")
                    if method == "GET" and path == "/auth/me" and response.ok:
                        self.me_uid = data.get("id")
                except (json.JSONDecodeError, ValueError):
                    pass
            return success

        except (requests.exceptions.ConnectionError, requests.exceptions.Timeout):
            self.results.append({
                "method": method, "path": path, "description": description,
                "status": "ERROR", "success": False,
            })
            self.log(f"  [ERR ] {method:7s} {path:45s} -> ERROR DE CONEXION")
            return False

    def print_section(self, title: str):
        self.log(f"{'='*60}", BLUE)
        self.log(f" {title}", BLUE)
        self.log(f"{'='*60}", BLUE)

    def test_public_routes(self):
        self.print_section("FASE 1: Rutas Publicas")
        self.test_route("GET", "/.well-known/assetlinks.json", "Asset links JSON")
        self.test_route("POST", "/auth/login", "Login (credenciales invalidas)",
            json_data={"email": "test@test.com", "passwd": "wrongpass"}, expected_status=400)
        ts = int(time.time())
        self.test_route("POST", "/auth/register", "Registro de usuario de prueba",
            json_data={"name": f"testuser_{ts}", "email": f"test_{ts}@test.com",
                       "passwd": "TestPass123!", "lang": "ESP", "utcOffset": 1.0},
            valid_statuses=[200, 409])
        self.test_route("POST", "/auth/verify", "Verify (email/codigo invalidos)",
            json_data={"email": "test@test.com", "code": "000000"}, expected_status=400)
        self.test_route("POST", "/auth/google/androidlogin", "Google Android Login (idToken vacio)",
            json_data={"idToken": ""}, expected_status=401)
        self.test_route("GET", "/groups/redirectInvite", "Redirect invite (sin params)",
            allow_redirects=False)

    def authenticate(self) -> bool:
        self.print_section("FASE 2: Autenticacion")
        ts = int(time.time())
        reg_email = f"test_{ts}@test.com"
        reg_pass = "TestPass123!"

        self.log(f"  Intentando registrar: testuser_{ts}", CYAN)
        try:
            response = self.make_request("POST", "/auth/register",
                json_data={"name": f"testuser_{ts}", "email": reg_email,
                           "passwd": reg_pass, "lang": "ESP", "utcOffset": 1.0})
            if response.status_code in (200, 409):
                self.log(f"  [PASS] Registro completado (status: {response.status_code})")
                self.test_user = {"email": reg_email, "passwd": reg_pass}
            else:
                self.log(f"  [INFO] Registro respondio {response.status_code}, intentando login...")
        except Exception:
            pass

        if not self.access_token:
            self.log(f"  Intentando login...", CYAN)
            for creds in [
                {"email": reg_email, "passwd": reg_pass},
                {"email": "admin@test.com", "passwd": "admin123"},
                {"email": "test@test.com", "passwd": "test123"},
            ]:
                try:
                    response = self.make_request("POST", "/auth/login", json_data=creds)
                    if response.status_code == 200:
                        data = response.json() if response.text else {}
                        self.access_token = data.get("jwtAccessToken")
                        self.refresh_token = data.get("jwtRefreshToken")
                        self.test_user = creds
                        self.log(f"  [PASS] Login exitoso con {creds['email']}")
                        break
                    else:
                        self.log(f"  [INFO] Login fallo con {creds['email']}: {response.status_code}")
                except Exception:
                    continue

        if self.access_token:
            self.log(f"  Token de acceso obtenido")
            return True
        else:
            self.log(f"  No se pudo obtener token de acceso. Las rutas protegidas daran 401/403.")
            return False

    def test_auth_routes(self):
        self.print_section("FASE 3: Rutas de Autenticacion")
        self.test_route("GET", "/auth/me", "Obtener perfil del usuario", use_auth=True)
        self.test_route("POST", "/auth/regenAccess", "Regenerar token de acceso",
            use_auth=True, use_refresh_token=True)
        self.test_route("PATCH", "/auth/editUser", "Editar usuario",
            use_auth=True, json_data={"uid": self.me_uid or 1, "nombreusu": f"edited_{int(time.time())}"})
        self.test_route("PATCH", "/auth/setEmailLogin", "Set email login",
            use_auth=True, json_data={"email": f"new_{int(time.time())}@test.com", "passwd": "NewPass123!"})

    def test_oauth_routes(self):
        self.print_section("FASE 4: Rutas OAuth")
        self.test_route("GET", "/auth/google/login", "Google OAuth login", allow_redirects=False)
        self.test_route("GET", "/auth/google/callback", "Google OAuth callback", allow_redirects=False)
        self.test_route("GET", "/auth/testHTML", "Test HTML")
        self.test_route("POST", "/auth/setOauth", "Set OAuth",
            use_auth=True, json_data={"providerUid": "test-provider-uid", "authProvider": "GOOGLE"})
        self.test_route("POST", "/auth/deleteOauth", "Delete OAuth",
            use_auth=True, json_data={"authProvider": "GOOGLE"})

    def test_avatar_routes(self):
        self.print_section("FASE 5: Rutas de Avatar")
        self.test_route("POST", "/avatar/", "Obtener avatar", use_auth=True)

    def test_group_routes(self):
        self.print_section("FASE 6: Rutas de Grupos")
        self.test_route("GET", "/group/userGroups", "Obtener grupos del usuario", use_auth=True)
        self.test_route("POST", "/groups/createGroup", "Crear grupo",
            use_auth=True, json_data={"name": f"Test Group {int(time.time())}", "desc": "Grupo de prueba"})
        gid = self.created_group_id or 999999
        self.test_route("POST", "/groups/inviteUrl", "Generar URL de invitacion",
            use_auth=True, json_data={"gid": gid})
        self.test_route("POST", "/groups/joinByUrl", "Unirse por URL",
            use_auth=True, json_data={"inviteUrl": "http://example.com/invite/abc123"})
        self.test_route("POST", "/groups/joinByCode", "Unirse por codigo",
            use_auth=True, json_data={"code": "INVALIDCODE"})
        self.test_route("POST", "/groups/invites", "Crear invitacion",
            use_auth=True, json_data={"gid": gid, "expiresInSeconds": 3600, "maxUses": 10})
        self.test_route("GET", f"/groups/{gid}/invites", "Obtener invitaciones de grupo", use_auth=True)
        self.test_route("POST", "/groups/invites/revoke", "Revocar invitacion",
            use_auth=True, json_data={"gid": gid, "code": "INVALIDCODE"})
        self.test_route("GET", f"/groups/{gid}/members", "Obtener miembros de grupo", use_auth=True)
        self.test_route("POST", "/groups/leave", "Salir de grupo",
            use_auth=True, json_data={"gid": gid})
        self.test_route("PATCH", "/groups/setMemberRole", "Set member role",
            use_auth=True, json_data={"gid": gid, "userId": 1, "role": 1})
        self.test_route("GET", f"/groups/{gid}/audit", "Obtener audit de grupo", use_auth=True)
        self.test_route("PATCH", "/groups/editGroup", "Editar grupo",
            use_auth=True, json_data={"gid": gid, "name": "Edited Group", "desc": "Descripcion editada"})
        self.test_route("PATCH", "/groups/passOwnership", "Transferir propiedad",
            use_auth=True, json_data={"gid": gid, "newOwnerUserId": 1})
        self.test_route("DELETE", "/groups/deleteGroup", "Eliminar grupo",
            use_auth=True, json_data={"gid": gid})
        self.test_route("POST", "/groups/addUser", "Anadir usuario a grupo",
            use_auth=True, json_data={"gid": gid, "userId": 1})
        self.test_route("POST", "/groups/removeUser", "Eliminar usuario de grupo",
            use_auth=True, json_data={"gid": gid, "userId": 1})

    def test_task_routes(self):
        self.print_section("FASE 7: Rutas de Tareas")
        gid = self.created_group_id or 999999
        self.test_route("POST", "/tasks", "Crear tarea", use_auth=True,
            json_data={"gid": gid, "titulo": "Test Task", "descripcion": "Descripcion de prueba",
                       "tipo": "UNICO", "prioridad": 1,
                       "extraUnico": {"fechaLimite": "2026-12-31T23:59:59Z"}})
        self.test_route("POST", f"/tasks/{gid}", "Listar tareas del grupo", use_auth=True)
        tid = self.created_task_id or 999999
        self.test_route("PATCH", f"/tasks/{tid}/complete", "Completar tarea", use_auth=True)
        self.test_route("PATCH", f"/tasks/{tid}/edit", "Editar tarea",
            use_auth=True, json_data={"titulo": "Edited Task", "prioridad": 2})
        self.test_route("PATCH", f"/tasks/{tid}/uncomplete", "Descompletar tarea", use_auth=True)
        self.test_route("DELETE", f"/tasks/{tid}/delete", "Eliminar tarea", use_auth=True)

    def test_store_routes(self):
        self.print_section("FASE 8: Rutas de Tienda")
        self.test_route("GET", "/store/items", "Obtener items", use_auth=True)
        self.test_route("GET", "/store/items/admin", "Obtener items admin", use_auth=True)
        self.test_route("POST", "/store/buy/1", "Comprar item", use_auth=True)
        self.test_route("POST", "/store/equip/1", "Equipar item", use_auth=True)

    def test_admin_routes(self):
        self.print_section("FASE 9: Rutas de Admin")
        self.test_route("POST", "/admin/cosmetic/upload", "Upload cosmetic (sin archivo)", use_auth=True)
        self.test_route("POST", "/admin/cosmetic/upload/1", "Upload cosmetic con ID", use_auth=True)
        self.test_route("DELETE", "/admin/cosmetic/delete/1", "Delete cosmetic", use_auth=True)
        self.test_route("POST", "/admin/users", "Admin users list", use_auth=True)
        self.test_route("GET", "/admin/users/1", "Get user by ID", use_auth=True)
        self.test_route("POST", "/admin/groups", "Admin groups list", use_auth=True)
        self.test_route("POST", "/admin/groups/add", "Admin add to group",
            use_auth=True, json_data={"name": "Admin Test Group", "uid": 1, "desc": "Admin test"})
        self.test_route("POST", "/admin/groups/edit/1", "Admin edit group",
            use_auth=True, json_data={"name": "Edited Admin Group", "desc": "Edited desc"})
        self.test_route("DELETE", "/admin/groups/delete/1", "Admin delete group", use_auth=True)
        self.test_route("POST", "/admin/groups/moredata/1", "Admin group more data", use_auth=True)
        ts = int(time.time())
        self.test_route("POST", "/admin/user/create", "Admin create user", use_auth=True,
            json_data={"name": f"admin_test_{ts}", "email": f"admin_test_{ts}@test.com",
                       "password": "TestPass123!", "lang": "ESP", "utcOffset": 1.0, "role": "USER"})
        self.test_route("POST", "/admin/user/delete/999999", "Admin delete user", use_auth=True)

    def test_static_routes(self):
        self.print_section("FASE 10: Rutas Estaticas")
        self.test_route("GET", "/static/", "Static resources", expected_status=404)
        self.test_route("GET", "/admin/", "Admin static UI")
        self.test_route("GET", "/assets/", "Assets directory", expected_status=404)

    def print_summary(self):
        self.print_section("RESUMEN")
        total = len(self.results)
        passed = sum(1 for r in self.results if r["success"])
        failed = total - passed
        self.log(f"  Total de rutas probadas: {total}")
        self.log(f"  Exitosas: {passed}")
        self.log(f"  Fallidas: {failed}")
        if failed > 0:
            self.log(f"  Rutas fallidas:")
            for r in self.results:
                if not r["success"]:
                    self.log(f"    [FAIL] {r['method']:7s} {r['path']:45s} -> {r['status']}")
        output_file = "test_results.json"
        with open(output_file, "w") as f:
            json.dump(self.results, f, indent=2)
        self.log(f"  Resultados guardados en: {output_file}")


class ProxyHandler(BaseHTTPRequestHandler):
    tester = None
    lock = threading.Lock()

    def do_GET(self):
        if self.path == "/" or self.path == "/index.html":
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            self.wfile.write(GUI_HTML.encode("utf-8"))
        elif self.path.startswith("/api/"):
            self.send_response(400)
            self.send_header("Content-Type", "application/json")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            self.wfile.write(json.dumps({"error": "Use POST for API"}).encode())
        else:
            self.send_response(404)
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            self.wfile.write(b"Not Found")

    def do_POST(self):
        content_length = int(self.headers.get("Content-Length", 0))
        body = self.rfile.read(content_length) if content_length else b"{}"
        try:
            data = json.loads(body) if body else {}
        except json.JSONDecodeError:
            data = {}

        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Headers", "*")
        self.send_header("Access-Control-Allow-Methods", "*")
        self.end_headers()

        parsed = urlparse(self.path)
        path = parsed.path

        with ProxyHandler.lock:
            if path == "/api/set_url":
                ProxyHandler.tester = RouteTester(data.get("url", "http://localhost:8080"))
                response = {"ok": True}

            elif path == "/api/run_phase":
                if not ProxyHandler.tester:
                    response = {"error": "No tester initialized", "logs": []}
                else:
                    phase = data.get("phase", "")
                    func = getattr(ProxyHandler.tester, phase, None)
                    if func:
                        ProxyHandler.tester.logs = []
                        try:
                            func()
                        except Exception as e:
                            ProxyHandler.tester.log(f"Error: {e}", RED)
                    response = {"logs": ProxyHandler.tester.logs}

            elif path == "/api/results":
                if not ProxyHandler.tester:
                    response = {"results": []}
                else:
                    response = {"results": ProxyHandler.tester.results}

            elif path == "/api/summary":
                if not ProxyHandler.tester:
                    response = {"total": 0, "passed": 0, "failed": 0}
                else:
                    total = len(ProxyHandler.tester.results)
                    passed = sum(1 for r in ProxyHandler.tester.results if r["success"])
                    failed = total - passed
                    response = {"total": total, "passed": passed, "failed": failed}

            elif path == "/api/export":
                if not ProxyHandler.tester:
                    response = {"data": []}
                else:
                    response = {"data": ProxyHandler.tester.results}

            elif path == "/api/clear":
                ProxyHandler.tester = RouteTester("http://localhost:8080")
                response = {"ok": True}

            else:
                response = {"error": "Unknown endpoint"}

        self.wfile.write(json.dumps(response).encode())

    def do_OPTIONS(self):
        self.send_response(204)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Headers", "*")
        self.send_header("Access-Control-Allow-Methods", "*")
        self.end_headers()

    def log_message(self, format, *args):
        pass


def run_gui(port: int = 9090, base_url: str = "http://localhost:8080"):
    ProxyHandler.tester = RouteTester(base_url)
    server = HTTPServer(("127.0.0.1", port), ProxyHandler)

    url = f"http://127.0.0.1:{port}"
    webbrowser.open(url)
    print(f"{BOLD}{CYAN}╔{'═'*58}╗{RESET}")
    print(f"{BOLD}{CYAN}║  Web GUI: {url}{RESET}")
    print(f"{BOLD}{CYAN}║  Backend: {base_url}{RESET}")
    print(f"{BOLD}{CYAN}║  Ctrl+C para salir{RESET}")
    print(f"{BOLD}{CYAN}╚{'═'*58}╝{RESET}")

    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nDeteniendo servidor...")
        server.shutdown()


def main():
    parser = argparse.ArgumentParser(description="Test all backend routes")
    parser.add_argument("--base-url", default="http://localhost:8080",
                        help="Base URL del backend (default: http://localhost:8080)")
    parser.add_argument("--gui", action="store_true", help="Launch web GUI instead of CLI")
    parser.add_argument("--port", type=int, default=9090, help="Port for web GUI (default: 9090)")
    args = parser.parse_args()

    if args.gui:
        run_gui(args.port, args.base_url)
    else:
        tester = RouteTester(args.base_url)

        print(f"{BOLD}{CYAN}╔{'═'*58}╗{RESET}")
        print(f"{BOLD}{CYAN}║  Testing Backend Routes - Astrais{RESET}")
        print(f"{BOLD}{CYAN}║  Base URL: {args.base_url}{RESET}")
        print(f"{BOLD}{CYAN}╚{'═'*58}╝{RESET}")

        print(f"  Verificando conexion a {args.base_url}...")
        try:
            response = requests.get(f"{args.base_url}/", timeout=5)
            print(f"  Servidor respondiendo (status: {response.status_code})")
        except requests.exceptions.ConnectionError:
            print(f"  No se puede conectar al servidor en {args.base_url}")
            print(f"  Asegurate de que el backend esta corriendo.")
            sys.exit(1)

        tester.test_public_routes()
        tester.authenticate()
        tester.test_auth_routes()
        tester.test_oauth_routes()
        tester.test_avatar_routes()
        tester.test_group_routes()
        tester.test_task_routes()
        tester.test_store_routes()
        tester.test_admin_routes()
        tester.test_static_routes()
        tester.print_summary()


if __name__ == "__main__":
    main()
