package com.example.nikita.qrtest1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class AsyncQRGenerator extends AsyncTask<String, Integer, ArrayList<Bitmap>> {

    Activity activity;

    public AsyncQRGenerator(Activity a) {
        this.activity = a;
    }

    public Bitmap img;
    private ProgressDialog dialog;

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(activity);
        dialog.setTitle("Generating...");
        dialog.setMessage("Generating QR code, please wait...");
        dialog.setCancelable(false);
        dialog.show();
        super.onPreExecute();
    }

    private static final int WIDTH = 200; /* ширина выходного изображения */

    @Override
    protected ArrayList<Bitmap> doInBackground(String... params) {
        ArrayList<Bitmap> list = new ArrayList<Bitmap>();
        for (int i = 0; i < params.length; i++) {
            try {
            /* Используется библиотека ZXing.
               Первый параметр - исходная строка.
               Второй параметр - формат кода (ZXing умеет не только QR)
               Третий и четвёртый - размер матрицы */
                BitMatrix matrix = new QRCodeWriter().encode(
                        params[i],
                        com.google.zxing.BarcodeFormat.QR_CODE,
                        WIDTH, WIDTH);
                /* Конвертируем матрицу битов в картинку */
                Bitmap bitmap = matrixToBitmap(matrix);
                img = bitmap;
                /* Сохраняем файл */
                saveBitmapAsImageFile(bitmap, String.valueOf(i + 1));
                list.add(bitmap);
                /* Сообщаем об очередном сгенерированном коде */
                publishProgress(i + 1, params.length);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    private Bitmap matrixToBitmap(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setPixel(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return image;
    }

    /* Сохраняет bitmap на SD-карту в файл с названием fileName.
   Возвращает true, если сохранение успешно, и false, если сохранить не удалось. */
    private boolean saveBitmapAsImageFile(Bitmap bitmap, String fileName) {
        /* Получаем путь до папки сохранения */
        String storagePath = Environment.getExternalStorageDirectory() + "/GroupLock/";
        File sdDir = new File(storagePath);
        /* Создаём директорию */
        sdDir.mkdirs();

        try {
            /* Создаём необходимые потоки */
            String filePath = sdDir.getPath() + "/" + fileName + ".png";
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);

            /* Сохраняем файл в формате .png */
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);

            /* Закрываем потоки */
            bos.flush();
            bos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        dialog.setMessage(values[0] + " of " + values[1] + " done...");
    }

    @Override
    protected void onPostExecute(ArrayList<Bitmap> bitmap) {
        try {
            /* Закрываем диалоговое окно */
            dialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Оповещаем пользователя об успешном завершении */
        String message = "QR codes generated successfully!";
        Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
        toast.show();

        MainActivity.img.setImageBitmap(bitmap.get(0));

        super.onPostExecute(bitmap);
    }


}
