package com.algorithmia.algo;

import com.algorithmia.APIException;
import com.algorithmia.client.HttpClient;
import com.algorithmia.client.HttpClientHelpers.AlgoResponseHandler;

import java.util.concurrent.Future;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Represents an Algorithmia algorithm that can be called.
 */
public class Algorithm {
    private AlgorithmRef algoRef;
    private HttpClient client;

    public Algorithm(HttpClient client, AlgorithmRef algoRef) {
        this.client = client;
        this.algoRef = algoRef;
    }

    /**
     * Calls the Algorithmia API on a given input.
     * Attempts to automatically format the input as JSON.
     *
     * @param input algorithm input, will automatically be converted into JSON
     * @return algorithm result (AlgoSuccess or AlgoFailure)
     * @throws APIException if there is a problem communication with the Algorithmia API.
     */
    public AlgoResponse pipe(Object input) throws APIException {
        final Gson gson = new Gson();
        final JsonElement inputJson = gson.toJsonTree(input);
        final AlgoResponse result = pipeJson(inputJson);
        return result;
    }

   /**
     * Calls the Algorithmia API on a given input asynchronously.
     * The future response will complete when the algorithm has completed or errored
     *
     * @param input algorithm input, will automatically be converted into JSON
     * @return future algorithm result (AlgoSuccess or AlgoFailure)
     */
    public FutureAlgoResponse pipeAsync(Object input) {
        final Gson gson = new Gson();
        final JsonElement inputJson = gson.toJsonTree(input);
        final FutureAlgoResponse result = new FutureAlgoResponse(this.pipeJsonAsync(inputJson));
        return result;
    }


    /**
     * Run an algorithm on JSON input directly
     * @param inputJson json input value
     * @return success or failure
     * @throws APIException if there is a problem communication with the Algorithmia API.
     */
    private AlgoResponse pipeJson(JsonElement inputJson) throws APIException {
        try {
            return pipeJsonAsync(inputJson).get();
        } catch(java.util.concurrent.ExecutionException e) {
            throw new APIException(e.getCause().getMessage());
        } catch(java.util.concurrent.CancellationException e) {
            throw new APIException("API connection cancelled: " + algoRef.getUrl() + " (" + e.getMessage() + ")", e);
        } catch(java.lang.InterruptedException e) {
            throw new APIException("API connection interrupted: " + algoRef.getUrl() + " (" + e.getMessage() + ")", e);
        }
    }

    /**
     * Run an algorithm on JSON input directly
     * @param inputJson json input value
     * @return success or failure
     */
    private Future<AlgoResponse> pipeJsonAsync(JsonElement inputJson) {
        return client.post(
            algoRef.getUrl(),
            new StringEntity(inputJson.toString(), ContentType.APPLICATION_JSON),
            new AlgoResponseHandler()
        );
    }


}
