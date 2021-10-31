package com.example.demo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Java11httpclientAsyncApplication {

	public static void main(String[] args) {
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "10");
		SpringApplication.run(Java11httpclientAsyncApplication.class, args);
	}

}

@RestController
class HelloWorldController {
	
	private static final ExecutorService executorService = Executors.newFixedThreadPool(5);
	private static final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(2))
			// This is optional if not provided HttpClient will create it's own cachedThreadpool
			.executor(executorService) 
			.build();
	
	@GetMapping("/sayHello/{name}")
	public Response sayHello(@PathVariable String name) throws InterruptedException {
		Thread.sleep(20000L);
		return new Response(name, new Random().nextInt(100));
	}
	
	@GetMapping("/sayHelloAysnc")
	public CompletableFuture<Response> sayHelloCompletableFuture() {
		HttpRequest httpRequest = HttpRequest
				.newBuilder(URI.create("http://localhost:8081/sayHello/" + new Random().nextInt(1000000)))
				.GET()
				.header("Accept", "application/json")
				.build();

		return httpClient.sendAsync(httpRequest, BodyHandlers.ofString())
		  .thenApply(res -> JsonUtil.fromJson(res.body(), Response.class));

//      The above one can also be written like this			
//		return httpClient.sendAsync(httpRequest, BodyHandlers.ofString())
//							  .thenApply(HttpResponse<String>::body)
//							  .thenApply(res -> JsonUtil.fromJson(res, Response.class));
	}
	
	@GetMapping("/sayHelloCombine/{name}")
	public CompletableFuture<List<Response>> sayHelloCombineCompletableFuture(@PathVariable String name) {
	HttpRequest httpRequest = HttpRequest
			.newBuilder(URI.create("http://localhost:8081/sayHello/" + name + new Random().nextInt(1000000)))
			.GET()
			.header("Accept", "application/json")
			.build();
	CompletableFuture<Response> responseHolder1 = 
		httpClient.sendAsync(httpRequest, BodyHandlers.ofString())
			.thenApply(res -> JsonUtil.fromJson(res.body(), Response.class));
	CompletableFuture<Response> responseHolder2 = 
			httpClient.sendAsync(httpRequest, BodyHandlers.ofString())
				.thenApply(res -> JsonUtil.fromJson(res.body(), Response.class));
	CompletableFuture<Response> responseHolder3 = 
			httpClient.sendAsync(httpRequest, BodyHandlers.ofString())
				.thenApply(res -> JsonUtil.fromJson(res.body(), Response.class));
	return responseHolder1.thenCompose(r1 -> 
	 	   responseHolder2.thenCombine(responseHolder3,
					(r2, r3)  -> Arrays.asList(r1, r2, r3)));
	
	}
}

record Response(String name, Integer age) {}