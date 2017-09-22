package com.abi.smartlogin.neuralnetwork;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.abi.smartlogin.db.DatabaseAccess;
import com.abi.smartlogin.entity.User;
import com.abi.smartlogin.util.ImageProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static com.abi.smartlogin.util.ImageProcessor.IMAGE_SIZE;

public class FaceRecognizer {

    private static final int ITERATION = 5;
    private static final int MAX_USERS = 10;
    private static final String TAG = "FaceRecognizer";
    private DatabaseAccess databaseAccess;
    private Context context;
    private MultiLayerPerceptron perceptron;
    private static FaceRecognizer faceRecognizer;

    private FaceRecognizer() {

    }

    public static FaceRecognizer getInstance() {
        if (faceRecognizer == null) {
            faceRecognizer = new FaceRecognizer();
        }
        return faceRecognizer;
    }

    public void open(Context context) {
        this.context = context;
        this.databaseAccess = DatabaseAccess.getInstance(context);
        this.databaseAccess.open();
    }

    public void close() {
        this.databaseAccess.close();
    }

    public double train(int userID, Bitmap bitmap) throws IOException, ClassNotFoundException {

        double error = 0.0;

        if (this.perceptron == null) {
            this.perceptron = this.openPerceptron();
        }

        double[] output = new double[MAX_USERS];
        double[] inputs = ImageProcessor.convertImage(bitmap, IMAGE_SIZE, IMAGE_SIZE);

        // Learning
        for (int i = 0; i < ITERATION; i++) {
            output[userID] = 1.0;
            error = perceptron.backPropagate(inputs, output);
        }

        return error;
    }

    public void save() throws IOException {
        this.write(this.context, this.perceptron);
    }

    public User find(Bitmap bitmap) throws IOException, ClassNotFoundException {

        // Face recognition
        double[] inputs = ImageProcessor.convertImage(bitmap, IMAGE_SIZE, IMAGE_SIZE);

        MultiLayerPerceptron perceptron = this.openPerceptron();
        double[] output = perceptron.execute(inputs);

        int id = 0;
        for (int i = 0; i < MAX_USERS; i++) {
            if (output[i] > output[id]) {
                id = i;
            }
        }

        Log.i(TAG, String.format("Face %d recognized with accuracy %.2f", id, output[id]));

        return this.databaseAccess.getUser(id);
    }

    private MultiLayerPerceptron openPerceptron() throws IOException, ClassNotFoundException {

        MultiLayerPerceptron perceptron = this.read(context);

        if (perceptron == null) {
            // For the first time only
            // IMAGE_SIZE x IMAGE_SIZE -> IMAGE_SIZE -> MAX_USERS
            int[] layers = new int[]{IMAGE_SIZE * IMAGE_SIZE, IMAGE_SIZE, MAX_USERS};
            perceptron = new MultiLayerPerceptron(layers, 0.6);
            this.write(context, perceptron);
        }
        return perceptron;
    }

    private void write(Context context, MultiLayerPerceptron obj) throws IOException {
        File dir = context.getExternalFilesDir(null);
        File filePath = new File(dir.getPath() + "/" + "perceptron");
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filePath));
        outputStream.writeObject(obj);
        outputStream.flush();
        outputStream.close();
    }

    private MultiLayerPerceptron read(Context context) {
        File dir = context.getExternalFilesDir(null);
        File filePath = new File(dir.getPath() + "/" + "perceptron");
        if (!filePath.exists()) {
            return null;
        }
        MultiLayerPerceptron perceptron = null;
        ObjectInputStream inputStream = null;
        try {
            inputStream = new ObjectInputStream(new FileInputStream(filePath));
            perceptron = (MultiLayerPerceptron) inputStream.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return perceptron;
    }
}
