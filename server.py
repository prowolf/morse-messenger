import socket
import threading

clients = set()
clients_lock = threading.Lock()

commands = ['t', 'f']


def broadcast_except(data, no_broadcast):
    with clients_lock:
        for c in clients - set(no_broadcast):
            c.sendall(data)


def broadcast(data):
    with clients_lock:
        print(clients)
        for c in clients:
            c.sendall(data)


def handler(current_connection, address):
    print(current_connection)
    with clients_lock:
        clients.add(current_connection)
    try:
        while True:
            data = current_connection.recv(2048)
            line = data.decode('UTF-8').strip()
            print(data)
            if line in commands:
                broadcast_except((line + "\n").encode(), [current_connection])
            elif not data:
                current_connection.shutdown(socket.SHUT_RDWR)
                current_connection.close()
                break
    finally:
        with clients_lock:
            clients.remove(current_connection)


def listen():
    connection = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    connection.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    connection.bind(('0.0.0.0', 8484))
    connection.listen(10)
    while True:
        current_connection, address = connection.accept()
        thread = threading.Thread(target=handler, args=(current_connection, address))
        thread.start()


if __name__ == "__main__":
    try:
        listen()
    except KeyboardInterrupt:
        pass
