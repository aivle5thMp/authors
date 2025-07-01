# 

## Model
www.msaez.io/#/106015923/storming/9b47f316724021cfcdf69e251b42a323

## Before Running Services
### Make sure there is a Kafka server running
```
cd kafka
docker-compose up
```
- Check the Kafka messages:
```
cd infra
docker-compose exec -it kafka /bin/bash
cd /bin
./kafka-console-consumer --bootstrap-server localhost:9092 --topic
```

## Run the backend micro-services
See the README.md files inside the each microservices directory:

- authors
- manuscripts
- aiservice
- authentication
- mybook
- points
- payments
- books
- notification


## Run API Gateway (Spring Gateway)
```
cd gateway
mvn spring-boot:run
```

## Test by API
- authors
```
 http :8088/authors id="id"userId="user_id"status="status"name="name"bio="bio"portfolioUrl="portfolio_url"
```
- manuscripts
```
 http :8088/manuscripts id="id"userId="user_id"title="title"content="content"status="status"
```
- aiservice
```
 http :8088/aiServices id="id"log="log"
```
- authentication
```
 http :8088/users id="id"name="name"email="email"password="password"role="role"isSubscribed="is_subscribed"
```
- mybook
```
 http :8088/myBooks id="id"userId="user_id"bookId="book_id"createdAt="created_at"
```
- points
```
 http :8088/points id="id"userId="user_id"point="point"totalPoint="total_point"createdAt="created_at"
```
- payments
```
 http :8088/payments id="id"userId="user_id"item="item"amount="amount"status="status"createdAt="created_at"
```
- books
```
 http :8088/books id="id"authorId="author_id"authorName="author_name"title="title"category="category"content="content"summary="summary"audioUrl="audio_url"imageUrl="image_url"todayCount="today_count"totalCount="total_count"createdAt="created_at"
```
- notification
```
 http :8088/알림 id="id"email="email"message="message"isRead="is_read"createdAt="created_at"
```


## Run the frontend
```
cd frontend
npm i
npm run serve
```

## Test by UI
Open a browser to localhost:8088

## Required Utilities

- httpie (alternative for curl / POSTMAN) and network utils
```
sudo apt-get update
sudo apt-get install net-tools
sudo apt install iputils-ping
pip install httpie
```

- kubernetes utilities (kubectl)
```
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
```

- aws cli (aws)
```
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

- eksctl 
```
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin
```
