include .env

DOCK=docker compose up --build --detach
MVNW=./mvnw
BUILD_DIR=./target
MODULE=inventory-0.0.1-SNAPSHOT

test:
	$(MVNW) test

app.build:
	$(MVNW) clean package -DskipTests
	
app.run: app.build
	java -jar $(BUILD_DIR)/$(MODULE).jar

app.dock: 
	$(DOCK) inventory

infra:
	$(DOCK) inventory-pg prometheus grafana 