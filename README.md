# Kubernetes-Deployment-of-Multi-Service-Application
# Distributed Programming Project

This repository contains the deployment of a multi-service application using Kubernetes. The application includes a backend service that interfaces with a MySQL database and a frontend service that communicates with the backend service to retrieve and display data.

## Table of Contents

1. [Introduction](#introduction)
2. [Environment Setup](#environment-setup)
3. [Backend Service Deployment](#backend-service-deployment)
4. [Frontend Service Deployment](#frontend-service-deployment)
5. [Security Implementation](#security-implementation)
6. [Integration and Testing](#integration-and-testing)
7. [Conclusion](#conclusion)

## Introduction

This project demonstrates deploying a distributed application using Kubernetes, which includes container orchestration, service discovery, and load balancing.

## Environment Setup

1. **Kubernetes**: Platform for automating deployment, scaling, and management of containerized applications.
2. **Minikube**: Tool for running a single-node Kubernetes cluster locally.
3. **Docker**: Platform for developing, shipping, and running applications in containers.
4. **Spring Boot**: Framework for building production-ready Java applications.
5. **MySQL**: Relational database management system.
6. **Istio**: Service mesh providing tools for managing microservices.
7. **Helm**: Kubernetes package manager.

## Backend Service Deployment

### MySQL Deployment

1. **Create MySQL Secrets**:
    ```yaml
    apiVersion: v1
    kind: Secret
    metadata:
      name: mysql-secret
    type: Opaque
    data:
      mysql-root-password: cGFzc3dvcmQ= # base64 encoded value of 'password'
    ```
    ```bash
    kubectl apply -f mysql-secret.yaml
    ```

2. **Configure Persistent Storage**:
    ```yaml
    apiVersion: v1
    kind: PersistentVolume
    metadata:
      name: mysql-pv
    spec:
      capacity:
        storage: 1Gi
      accessModes:
        - ReadWriteOnce
      hostPath:
        path: "/mnt/data"
    ---
    apiVersion: v1
    kind: PersistentVolumeClaim
    metadata:
      name: mysql-pvc
    spec:
      accessModes:
        - ReadWriteOnce
      resources:
        requests:
          storage: 1Gi
    ```
    ```bash
    kubectl apply -f mysql-storage.yaml
    ```

3. **Deploy MySQL**:
    ```yaml
    apiVersion: apps/v1
    kind: Deployment
    metadata:
      name: mysql
    spec:
      selector:
        matchLabels:
          app: mysql
      strategy:
        type: Recreate
      template:
        metadata:
          labels:
            app: mysql
        spec:
          containers:
          - image: mysql:5.6
            name: mysql
            env:
            - name: MYSQL_ROOT_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: mysql-secret
                  key: mysql-root-password
            ports:
            - containerPort: 3306
              name: mysql
            volumeMounts:
            - name: mysql-persistent-storage
              mountPath: /var/lib/mysql
          volumes:
          - name: mysql-persistent-storage
            persistentVolumeClaim:
              claimName: mysql-pvc
    ---
    apiVersion: v1
    kind: Service
    metadata:
      name: mysql
    spec:
      ports:
      - port: 3306
      selector:
        app: mysql
    ```
    ```bash
    kubectl apply -f mysql-deployment.yaml
    ```

### Spring Boot Backend Service

1. **Configure Spring Boot Application**:
    ```properties
    spring.datasource.url=jdbc:mysql://mysql:3306/db_example
    spring.datasource.username=root
    spring.datasource.password=password
    spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL5Dialect
    ```

2. **Create Docker Image for Backend**:
    ```dockerfile
    FROM openjdk:11
    VOLUME /tmp
    COPY target/myservice.jar myservice.jar
    ENTRYPOINT ["java","-jar","/myservice.jar"]
    ```
    ```bash
    docker build -t your-dockerhub-username/myservice .
    docker push your-dockerhub-username/myservice
    ```

3. **Deploy Backend Service to Kubernetes**:
    ```yaml
    apiVersion: apps/v1
    kind: Deployment
    metadata:
      name: back-end-deployment
    spec:
      replicas: 1
      selector:
        matchLabels:
          app: back-end
      template:
        metadata:
          labels:
            app: back-end
        spec:
          containers:
          - name: back-end-container
            image: your-dockerhub-username/myservice:latest
            ports:
            - containerPort: 8080
    ---
    apiVersion: v1
    kind: Service
    metadata:
      name: back-end-service
    spec:
      ports:
        - name: http
          targetPort: 8080
          port: 80
      type: ClusterIP
      selector:
        app: back-end
    ```
    ```bash
    kubectl apply -f backend-deployment.yaml
    ```

## Frontend Service Deployment

1. **Configure Spring Boot Frontend Application**:
    ```yaml
    backEndURL: http://back-end-service/hello
    ```

   **Frontend Controller**:
    ```java
    package com.example.FrontEnd;

    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestMethod;
    import org.springframework.web.bind.annotation.RestController;
    import org.springframework.web.client.RestTemplate;

    @RestController
    public class MyWebService {

        @Value("${backEndURL}")
        String backEndURL;

        @RequestMapping(path = "/hello", method = RequestMethod.GET)
        public String hello() {
            try {
                RestTemplate restTemplate = new RestTemplate();
                String s = restTemplate.getForObject(backEndURL, String.class);
                return "Hello (from the front end) " + s + " (from the back end)";
            } catch (Exception e) {
                return e.getLocalizedMessage();
            }
        }
    }
    ```

2. **Create Docker Image for Frontend**:
    ```dockerfile
    FROM openjdk:11
    VOLUME /tmp
    COPY target/frontend.jar frontend.jar
    ENTRYPOINT ["java","-jar","/frontend.jar"]
    ```
    ```bash
    docker build -t your-dockerhub-username/frontend .
    docker push your-dockerhub-username/frontend
    ```

3. **Deploy Frontend Service to Kubernetes**:
    ```yaml
    apiVersion: apps/v1
    kind: Deployment
    metadata:
      name: front-end-deployment
    spec:
      replicas: 1
      selector:
        matchLabels:
          app: front-end
      template:
        metadata:
          labels:
            app: front-end
        spec:
          containers:
          - name: front-end-container
            image: your-dockerhub-username/frontend:latest
            ports:
            - containerPort: 8080
    ---
    apiVersion: v1
    kind: Service
    metadata:
      name: front-end-service
    spec:
      type: NodePort
      ports:
        - name: http
          port: 80
          targetPort: 8080
          nodePort: 30001
      selector:
        app: front-end
    ```
    ```bash
    kubectl apply -f frontend-deployment.yaml
    ```

4. **Accessing the Frontend Service**:
    ```bash
    kubectl port-forward svc/front-end-service 8081:80
    ```
    Access the frontend service using `http://localhost:8081/hello` in your web browser.

## Security Implementation

1. **Download and Install Istio**:
    ```bash
    curl -L https://istio.io/downloadIstio | sh -
    cd istio-*
    export PATH=$PWD/bin:$PATH
    istioctl install --set profile=demo -y
    ```

2. **Enable Automatic Sidecar Injection**:
    ```bash
    kubectl label namespace default istio-injection=enabled
    ```

3. **Role-Based Access Control (RBAC)**:
    Create and apply `role.yaml` and `rolebinding.yaml`:
    ```yaml
    apiVersion: rbac.authorization.k8s.io/v1
    kind: Role
    metadata:
      namespace: default
      name: pod-reader
    rules:
    - apiGroups: [""]
      resources: ["pods"]
      verbs: ["get", "watch", "list"]
    ---
    apiVersion: rbac.authorization.k8s.io/v1
    kind: RoleBinding
    metadata:
      name: read-pods
      namespace: default
    subjects:
    - kind: User
      name: "jane"
      apiGroup: rbac.authorization.k8s.io
    roleRef:
      kind: Role
      name: pod-reader
      apiGroup: rbac.authorization.k8s.io
    ```
    ```bash
    kubectl apply -f role.yaml
    kubectl apply -f rolebinding.yaml
    ```

4. **Mutual TLS (mTLS) Encryption**:
    Create and apply `peerauthentication.yaml`:
    ```yaml
    apiVersion: security.istio.io/v1beta1
    kind: PeerAuthentication
    metadata:
      name: default
      namespace: default
    spec:
      mtls:
        mode: STRICT
    ```
    ```bash
    kubectl apply -f peerauthentication.yaml
    ```

## Integration and Testing

1. **Verifying Access**:
    Use port-forwarding to access the services internally:
    ```bash
    kubectl port-forward svc/back-end-service 8080:80
    kubectl port-forward svc/front-end-service 8081:80
    ```
    Access the services via `http://localhost:8080/hello` and `http://localhost:8081/hello`.

2. **Ensuring Communication Between Frontend and Backend**:
    Verify that the frontend service can fetch data from the backend service and display it correctly.
    Check the frontend logs:
    ```bash
    kubectl logs <frontend-pod-name>
    ```

## Conclusion

### Summary of Achievements

- Deploying and managing a MySQL database within a Kubernetes cluster.
- Developing and deploying a Spring Boot backend service that interacts with the MySQL database.
- Developing and deploying a Spring Boot frontend service that communicates with the backend service.
- Configuring Kubernetes services for inter-service communication.
- Implementing security measures using Istio, RBAC, and mTLS encryption.
- Ensuring the application is functional and secure, providing a seamless user experience.

### Future Work and Improvements

- Enhancing the frontend UI for better user experience.
- Implementing CI/CD pipelines for automated deployment and updates.
- Adding more comprehensive security measures and monitoring.
- Scaling the application to handle higher loads and ensuring high availability.
