from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/take_photo', methods=['POST'])
def take_photo():
    import socket
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.connect(('10.0.0.35', 12345))
            s.sendall(b'TAKE_PHOTO')
            response = s.recv(1024)
            print(f"Received: {response.decode('utf-8')}")
            return jsonify({"status": "success", "message": "Photo command sent."})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)})
    #return jsonify({"status": "success", "message": "Photo command sent."})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
