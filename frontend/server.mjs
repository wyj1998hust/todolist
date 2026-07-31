import { createReadStream, promises as fs } from 'node:fs'
import { createServer, request as httpRequest } from 'node:http'
import { request as httpsRequest } from 'node:https'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const currentDirectory = path.dirname(fileURLToPath(import.meta.url))
const staticDirectory = path.resolve(process.env.STATIC_DIR || path.join(currentDirectory, 'dist'))
const port = Number(process.env.PORT || 8080)
const apiProxyTarget = process.env.API_PROXY_TARGET || ''
const mimeTypes = {
  '.css': 'text/css; charset=utf-8', '.html': 'text/html; charset=utf-8', '.ico': 'image/x-icon',
  '.js': 'text/javascript; charset=utf-8', '.json': 'application/json; charset=utf-8', '.map': 'application/json; charset=utf-8',
  '.png': 'image/png', '.svg': 'image/svg+xml', '.woff': 'font/woff', '.woff2': 'font/woff2',
}

function send(response, status, body, type = 'text/plain; charset=utf-8') {
  response.writeHead(status, { 'Content-Type': type, 'Cache-Control': 'no-store' })
  response.end(body)
}

async function resolveFile(requestPath) {
  const decoded = decodeURIComponent(requestPath)
  const normalized = path.posix.normalize(decoded).replace(/^\/+/, '')
  const candidate = path.resolve(staticDirectory, normalized || 'index.html')
  if (!candidate.startsWith(`${staticDirectory}${path.sep}`) && candidate !== path.join(staticDirectory, 'index.html')) return null
  try {
    const stat = await fs.stat(candidate)
    return stat.isFile() ? candidate : null
  } catch {
    return null
  }
}

const server = createServer(async (request, response) => {
  const requestUrl = new URL(request.url || '/', `http://${request.headers.host || 'localhost'}`)
  if (requestUrl.pathname === '/health') return send(response, 200, JSON.stringify({ status: 'UP' }), 'application/json; charset=utf-8')
  if (requestUrl.pathname.startsWith('/api/') && apiProxyTarget) return proxyApi(request, response)
  if (!['GET', 'HEAD'].includes(request.method || 'GET')) return send(response, 405, 'Method Not Allowed')

  let file = await resolveFile(requestUrl.pathname)
  if (!file && !path.extname(requestUrl.pathname)) file = path.join(staticDirectory, 'index.html')
  if (!file) return send(response, 404, 'Not Found')

  const extension = path.extname(file).toLowerCase()
  response.writeHead(200, {
    'Content-Type': mimeTypes[extension] || 'application/octet-stream',
    'Cache-Control': extension === '.html' ? 'no-store' : 'public, max-age=31536000, immutable',
  })
  if (request.method === 'HEAD') return response.end()
  createReadStream(file).pipe(response)
})

function proxyApi(request, response) {
  const target = new URL(request.url || '/', apiProxyTarget)
  const proxy = (target.protocol === 'https:' ? httpsRequest : httpRequest)(target, {
    method: request.method,
    headers: { ...request.headers, host: target.host },
  }, (upstream) => {
    response.writeHead(upstream.statusCode || 502, upstream.headers)
    upstream.pipe(response)
  })
  proxy.on('error', () => send(response, 502, 'API service unavailable'))
  request.pipe(proxy)
}

server.listen(port, '0.0.0.0', () => {
  console.log(`Vue static server listening on ${port}`)
})
