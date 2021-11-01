# This project uses Java 17 and demonstrates HttpClient async methods for asynchronous processing 


### ForkJoinPool
* Most of the processing happens in ForkJoin CommonPool.
* Set the ForkJoinPool commonPool Size to 10 (threads) using the property 
* -Djava.util.concurrent.ForkJoinPool.common.parllelism = 10
* Also we can use the System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "10");
  The above line should be placed as first line your code.
* We can verify the ForkJoinPool size by ForkJoinPool.commonPool().getParallelism()

### Creating HttpClient

	private static final ExecutorService executorService = Executors.newFixedThreadPool(5);
	private static final HttpClient httpClient = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(2)			
		// This is optional if not provided HttpClient will create it's own cachedThreadpool
		// using Executors.newCachedThreadPool();
		.executor(executorService) 
		.build();

### Dependent tasks of HttpRequest runs by default in ForkJoin CommonPool
	
	httpClient.sendAsync(httpRequest, BodyHandlers.ofString())
					  .thenApplyAsync(HttpResponse<String>::body);

Above line runs in ForkJoin CommonPool unless we provide the ExecutorService(thread pool) like this.

	httpClient.sendAsync(httpRequest, BodyHandlers.ofString())
					  .thenApplyAsync(HttpResponse<String>::body, executorService);

### Start the sayHello service
java -jar java -jar target\java11httpclient-async-0.0.1-SNAPSHOT.jar --server.port=8081 --server.tomcat.threads.max=500

Url http://localhost:8081/sayHello/{name} -> has a thread sleep of 20 seconds to simulate I/O delay.

### Start the sayHello proxy service which uses completable future
java -jar java -jar target\java11httpclient-async-0.0.1-SNAPSHOT.jar --server.port=8080 --server.tomcat.threads.max=10d

* Url http://localhost:8081/sayHelloAysnc
* Method sayHelloCompletableFuture -> Makes call to http://localhost:8081/sayHello/{name} in async manner

### How to combine more than 2 Completable futures?

	CompletableFuture<Response> responseHolder1 = 
		httpClient.sendAsync(httpRequest, BodyHandlers.ofString())
			.thenApply(res -> JsonUtil.fromJson(res.body(), Response.class));
	CompletableFuture<Response> responseHolder2 = 
			httpClient.sendAsync(httpRequest, BodyHandlers.ofString())
				.thenApply(res -> JsonUtil.fromJson(res.body(), Response.class));
	CompletableFuture<Response> responseHolder3 = 
			httpClient.sendAsync(httpRequest, BodyHandlers.ofString())
				.thenApply(res -> JsonUtil.fromJson(res.body(), Response.class));
	CompletableFuture<List<Response>> response = responseHolder1.thenCompose(r1 -> 
	 	   responseHolder2.thenCombine(responseHolder3,
					(r2, r3)  -> Arrays.asList(r1, r2, r3)));

### Generating load or doing load test.

* You can run the test case method HttpClientTest#send200Requests to generate load.
* You can also use Jmeter
