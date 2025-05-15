package com.example.stin_news;


import clients.AlphaVantageClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class AlphaVantageClientTest {

    @InjectMocks
    private AlphaVantageClient alphaVantageClient;

    @Mock
    private WebClient webClientMock;

    @Mock
    private RequestHeadersUriSpec requestHeadersUriSpecMock;

    @Mock
    private ResponseSpec responseSpecMock;

    private final String ticker = "AAPL";
    private final String fromDateTime = "2024-01-01";
    private final String toDateTime = "2024-01-07";
    private final String sort = "latest";
    private final int limit = 5;

    @Test
    void getCompanyNews_basicTest_returnsString() {
        String expectedResponse = "{\"feed\": []}";
        mockWebClientSuccess(expectedResponse);

        Mono<String> actualResponseMono = alphaVantageClient.getCompanyNews(ticker, fromDateTime, toDateTime, sort, limit);
        String actualResponse = actualResponseMono.block();

        assertEquals(expectedResponse, actualResponse);
        verify(webClientMock, times(1)).get();
    }

    @Test
    void getCompanyNews_withData_returnsString() {
        String expectedResponse = "{\"feed\": [{\"title\": \"News 1\"}]}";
        mockWebClientSuccess(expectedResponse);

        Mono<String> actualResponseMono = alphaVantageClient.getCompanyNews(ticker, fromDateTime, toDateTime, sort, limit);
        String actualResponse = actualResponseMono.block();

        assertEquals(expectedResponse, actualResponse);
        verify(webClientMock, times(1)).get();
    }

    /*@Test
    void getCompanyNews_temporaryErrorThenSuccess_returnsStringAfterRetry() {
        String firstResponse = "{\"information\": \"Thank you for using Alpha Vantage! Our standard API call frequency limit is 25 API requests per day and 5 API requests per minute.\"}";
        String successResponse = "{\"feed\": []}";

        when(webClientMock.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(any(Function.class))).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(String.class))
                .thenReturn(Mono.just(firstResponse)) // Simulate a retryable error message
                .thenReturn(Mono.just(successResponse));

        Mono<String> actualResponseMono = alphaVantageClient.getCompanyNews(ticker, fromDateTime, toDateTime, sort, limit);
        String actualResponse = actualResponseMono.block();

        assertEquals(successResponse, actualResponse);
        verify(webClientMock, times(2)).get(); // Expect 2 attempts due to retry
    }*/

    /*@Test
    void getCompanyNews_repeatedErrorsExceedingLimit_throwsException() {
        String errorResponse = "{\"information\": \"API call frequency limit exceeded\"}";

        when(webClientMock.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(any(Function.class))).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(String.class)).thenReturn(Mono.just(errorResponse));

        Mono<String> actualResponseMono = alphaVantageClient.getCompanyNews(ticker, fromDateTime, toDateTime, sort, limit);

        // Assuming your retry logic will eventually throw an exception after max attempts
        assertThrows(RuntimeException.class, actualResponseMono::block);
        verify(webClientMock, times(3)).get(); // Assuming ATTEMPTS is 3 for this test
    }*/

    /*@Test
    void getCompanyNews_httpError_propagatesException() {
        when(webClientMock.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(any(Function.class))).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(String.class)).thenReturn(Mono.error(WebClientResponseException.create(500, "Internal Server Error", null, null, null)));

        Mono<String> actualResponseMono = alphaVantageClient.getCompanyNews(ticker, fromDateTime, toDateTime, sort, limit);

        assertThrows(WebClientResponseException.class, actualResponseMono::block);
        verify(webClientMock, times(1)).get(); // No retry expected for non-retryable errors by default
    }*/

    private void mockWebClientSuccess(String responseBody) {
        when(webClientMock.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(any(Function.class))).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(String.class)).thenReturn(Mono.just(responseBody));
    }

}