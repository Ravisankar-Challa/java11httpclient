package com.example.demo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class HttpClientTest {

	private static final ExecutorService executorService = Executors.newFixedThreadPool(5);
	private static final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(2))
			// This is optional if not provided HttpClient will create it's own cachedThreadpool
			.executor(executorService) 
			.build();
	
	@Test
	void send200Requests() throws InterruptedException {
//		System.out.println(ForkJoinPool.commonPool().getParallelism());
//		System.out.println(Runtime.getRuntime().availableProcessors());
//		System.out.println(ForkJoinPool.commonPool().getActiveThreadCount());
//		To change the ForkJoinPool size define the system property
//		System.setProperty("java.util.concurrent.ForkJoinPool.commom.parallelism", 20);
//		Or else we can run the parllelstream in a custom forkjoin pool
//		ForkJoinPool forkJoinPool = new ForkJoinPool(10);
//	    Runnable r = () -> IntStream
//	            .range(-42, +42)
//	            .parallel()
//	            .map(i -> Thread.activeCount())
//	            .max()
//	            .ifPresent(System.out::println);
//
//	    ForkJoinPool.commonPool().submit(r).join();
//	    new ForkJoinPool(42).submit(r).join();
		for(int i = 0; i < 200; i++) {
			HttpRequest httpRequest = HttpRequest
				.newBuilder(URI.create("http://localhost:8080/sayHello1/" + new Random().nextInt(1000000)))
				.GET()
				.header("Accept", "application/json")
				.build();
			CompletableFuture<String> responseHolder = 
					httpClient.sendAsync(httpRequest, BodyHandlers.ofString())
							  .thenApplyAsync(r -> {
								  System.out.println(Thread.currentThread().getName()); 
								  return r.body();
							}, executorService);
// The above line can also be written as 
//thenApplyAsync(res -> res.body(), executorService);
// It is quite important to sent thread pool(executor Service) here
// other wise dependent tasks like thenApply or thenAccept will in run in ForkJoin common pool
			responseHolder.thenAcceptAsync(response -> {
				System.out.println(Thread.currentThread().getName()); 
				System.out.println(JsonUtil.toJson(response));}, executorService);
		}
		Thread.sleep(2000000L);
		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.SECONDS);
	}
	
	@Test
	void testCompletableFutureCombineAndCompose() throws InterruptedException {
		HttpRequest httpRequest = HttpRequest
				.newBuilder(URI.create("http://localhost:8081/sayHello/" + new Random().nextInt(1000000)))
				.GET()
				.header("Accept", "application/json")
				.build();
		CompletableFuture<String> responseHolder1 = 
			httpClient.sendAsync(httpRequest, BodyHandlers.ofString())
					  .thenApplyAsync(HttpResponse<String>::body, executorService);
		CompletableFuture<String> responseHolder2 = 
				httpClient.sendAsync(httpRequest, BodyHandlers.ofString())
						  .thenApplyAsync(HttpResponse<String>::body, executorService);
		CompletableFuture<String> responseHolder3 = 
				httpClient.sendAsync(httpRequest, BodyHandlers.ofString())
						  .thenApplyAsync(HttpResponse<String>::body, executorService);
		CompletableFuture<List<String>> result = responseHolder1.thenCompose(r1 -> 
		responseHolder2.thenCombine(responseHolder3,
						(r2, r3)  -> Arrays.asList(r1, r2, r3)));
		result.thenAccept(System.out::println);
		Thread.sleep(1000000L);
	}
	
}
