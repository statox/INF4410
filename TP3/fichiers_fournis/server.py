import sys
import BaseHTTPServer
import SimpleHTTPServer
import socket
import time
from urlparse import urlparse

def SplitParam(param):
	split_param = param.split('=')
	if len(split_param) != 2:
	  return ['', '']
	return split_param

class MyServer(BaseHTTPServer.BaseHTTPRequestHandler):
  def do_GET(self):
    query = urlparse(self.path).query
    query_params = dict(SplitParam(param) for param in query.split('&'))
    
    if 'nom' not in query_params:
      query_params['nom'] = 'Inconnu'
    nom = query_params['nom']

    time.sleep(0.75)

    self.send_response(200)
    self.send_header('Content-type','text/html')
    self.end_headers()
    self.wfile.write('Salut ' + nom + '. Je suis ' + socket.gethostname() + '.')
    return

server = BaseHTTPServer.HTTPServer
server_address = ('', 8000)

MyServer.protocol_version = 'HTTP/1.0'
httpd = server(server_address, MyServer)

sa = httpd.socket.getsockname()
print 'Serveur actif a ', sa[0], 'port', sa[1]
httpd.serve_forever()

