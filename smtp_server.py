#!/usr/bin/env python3
import socketserver
import threading
import time

class SMTPHandler(socketserver.BaseRequestHandler):
    def handle(self):
        client_ip = self.client_address[0]
        print(f"📧 SMTP connection from: {client_ip}")
        
        try:
            self.request.sendall(b"220 localhost ESMTP Test Server\r\n")
            
            while True:
                data = self.request.recv(1024).decode().strip()
                if not data:
                    break
                    
                print(f"📨 Received: {data}")
                
                if data.upper().startswith("QUIT"):
                    self.request.sendall(b"221 Bye\r\n")
                    break
                elif data.upper().startswith("EHLO") or data.upper().startswith("HELO"):
                    self.request.sendall(b"250-localhost\r\n250 OK\r\n")
                elif data.upper().startswith("MAIL FROM"):
                    self.request.sendall(b"250 OK\r\n")
                elif data.upper().startswith("RCPT TO"):
                    self.request.sendall(b"250 OK\r\n")
                elif data.upper().startswith("DATA"):
                    self.request.sendall(b"354 End data with <CR><LF>.<CR><LF>\r\n")
                    email_data = ""
                    while True:
                        line = self.request.recv(1024).decode()
                        email_data += line
                        if line.endswith("\r\n.\r\n"):
                            break
                    print("=" * 60)
                    print("🎯 EMAIL CAPTURED SUCCESSFULLY!")
                    print("=" * 60)
                    print(email_data)
                    print("=" * 60)
                    self.request.sendall(b"250 OK - Message received for testing\r\n")
                else:
                    self.request.sendall(b"250 OK\r\n")
                    
        except Exception as e:
            print(f"❌ Error: {e}")

def start_smtp_server(port=1028):
    with socketserver.TCPServer(("localhost", port), SMTPHandler) as server:
        print(f"🚀 SMTP Test Server running on localhost:{port}")
        print(f"📧 Spring Boot should connect to: localhost:{port}")
        print("⏳ Waiting for emails...")
        server.serve_forever()

if __name__ == "__main__":
    start_smtp_server(1028)
