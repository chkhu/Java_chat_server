import socket
import threading
import random
import string
import time

# 定义测试常量
NUM_MESSAGES = 60000
NUM_CLIENTS = 3
MESSAGE_MIN_LENGTH = 10
MESSAGE_MAX_LENGTH = 50
HOST = 'localhost'
PORT = 3296

def random_string(length):
    """生成一个指定长度的随机字符串"""
    letters = string.ascii_letters + string.digits
    return ''.join(random.choice(letters) for i in range(length))

def client_task():
    """客户端任务，发送指定数量的随机消息"""
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
        sock.connect((HOST, PORT))
        client_id = sock.recv(1024).decode()
        print(f"Connected as Client {client_id}")

        for _ in range(NUM_MESSAGES):
            message_length = random.randint(MESSAGE_MIN_LENGTH, MESSAGE_MAX_LENGTH)
            message = random_string(message_length)
            sock.sendall(f"Client {client_id} broadcasting: {message}".encode())

        print(f"Client {client_id} finished sending messages")

# 记录开始时间
start_time = time.time()

# 创建并启动指定数量的客户端线程
threads = []
for _ in range(NUM_CLIENTS):
    thread = threading.Thread(target=client_task)
    thread.start()
    threads.append(thread)

# 等待所有线程完成
for thread in threads:
    thread.join()

# 记录结束时间并计算总耗时
end_time = time.time()
total_time = end_time - start_time
print(f"All messages sent in {total_time:.2f} seconds.")
