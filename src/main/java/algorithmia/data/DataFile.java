package algorithmia.data;

import algorithmia.AlgorithmiaConf;
import algorithmia.APIException;
import algorithmia.client.*;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.HttpResponse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.io.IOException;
import org.apache.commons.io.IOUtils;

// /**
//  * A singular piece of data
//  */
public class DataFile extends DataObject {

    public DataFile(HttpClient client, String dataUrl) {
        super(client, dataUrl);
    }

     /**
      * Returns whether the file exists in the Data API
      * @return true iff the file exists
      * @throws APIException if there were any problems communicating with the DataAPI
      */
    public boolean exists() throws APIException {
        HttpResponse response = this.client.head(url());
        int status = response.getStatusLine().getStatusCode();
        if(status != 200 && status != 404) {
            throw APIException.fromHttpResponse(response, null);
        }
        return (200 == status);
    }

    /**
     * Returns the data as a raw file
     * @return the data as a local temporary file
     * @throws IOException if there were any problems communicating with the DataAPI
     */
    public File getFile() throws IOException {
        File tempFile = File.createTempFile(getName(), null);
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        IOUtils.copy(getInputStream(), outputStream);
        return tempFile;
    }


    public InputStream getInputStream() throws APIException, IOException {
        final HttpResponse response = client.get(url());
        HttpClientHelpers.assertStatusSuccess(response);
        return response.getEntity().getContent();
    }

    /**
     * Gets the data for this file as as string
     * @return the data as a String
     * @throws IOException if there were any problems communicating with the DataAPI
     */
    public String getString() throws IOException {
        return IOUtils.toString(getInputStream(), "UTF-8");
    }

    /**
     * Gets the data for this file as as string
     * @return the data as a String
     * @throws IOException if there were any problems communicating with the DataAPI
     */
    public byte[] getBytes() throws IOException {
        return IOUtils.toByteArray(getInputStream());
    }

    /**
     * Upload string data to this file as text
     * @param data the data to upload
     * @throws APIException if there were any problems communicating with the DataAPI
     */
    public void put(String data) throws APIException {
        HttpResponse response = client.put(url(), new StringEntity(data, ContentType.DEFAULT_TEXT));
        HttpClientHelpers.assertStatusSuccess(response);
    }

    /**
     * Upload raw data to this file as binary
     * @param data the data to upload
     * @throws APIException if there were any problems communicating with the DataAPI
     */
    public void put(byte[] data) throws APIException {
        HttpResponse response = client.put(url(), new ByteArrayEntity(data, ContentType.APPLICATION_OCTET_STREAM));
        HttpClientHelpers.assertStatusSuccess(response);
    }

    /**
     * Upload raw data to this file as an input stream
     * @param is the input stream of data to upload
     * @throws APIException if there were any problems communicating with the DataAPI
     */
    public void put(InputStream is) throws APIException {
        HttpResponse response = client.put(url(), new InputStreamEntity(is));
        HttpClientHelpers.assertStatusSuccess(response);
    }

    /**
     * Upload new data to this file from an existing file. These is a convenience wrapper for using InputStream.
     * @param file the file to upload data from
     * @throws APIException if there were any problems communicating with the DataAPI
     */
    public void put(File file) throws APIException, java.io.FileNotFoundException {
        put(new FileInputStream(file));
    }

    /**
     * Deletes this file.
     * @throws APIException if there were any problems communicating with the DataAPI
     */
    public void delete() throws APIException {
        HttpResponse response = this.client.delete(this.url());
        HttpClientHelpers.assertStatusSuccess(response);
    }

}