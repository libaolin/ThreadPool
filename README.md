ThreadPool
========
easy and simple use for Android.<br/>

ThreadPool in 3 steps
-------------------
1. New a ThreadPool instance:

    ```java  
    private ThreadPool mThreadPool;

    mThreadPool = new ThreadPool();
    ```

2. Define a Job:
    a Job will run in a thread:  

    ```java
    ThreadPool.Job<String> job = new ThreadPool.Job<String>() {
            @Override
            public String run(ThreadPool.JobContext jc) {
                /*You can do your work here*/
                return "hello-job";
            }
        };
    ```
    You can modify return value type in <>, example return a String


3. Submit Job:

   ```java
    mThreadPool.submit(job);
   ```
	You can also add a callback when the Job is done
	
	```java
    mThreadPool.submit(job, new FutureListener<String>() {
            @Override
            public void onFutureDone(Future<String> future) {
            }
        });
	```

Add ThreadPool to your project
----------------------------

Via Gradle:
```gradle
implementation 'com.droid.concurrent:threadpool:2.0.0'
```

Via Maven:
```xml
<dependency>
  <groupId>com.droid.concurrent</groupId>
  <artifactId>threadpool</artifactId>
  <version>2.0.0</version>
  <type>pom</type>
</dependency>
```
