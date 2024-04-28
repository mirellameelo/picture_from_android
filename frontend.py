import requests

def request_take_photo():
    url = 'http://localhost:5000/take_photo'
    response = requests.post(url)
    # print(response.json())

if __name__ == '__main__':
    request_take_photo()