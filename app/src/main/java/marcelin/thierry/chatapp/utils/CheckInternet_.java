package marcelin.thierry.chatapp.utils;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class CheckInternet_ extends AsyncTask<Void,Void,Boolean> {

    private Consumer mConsumer;
    public  interface Consumer { void accept(Boolean internet); }

    public  CheckInternet_(Consumer consumer) { mConsumer = consumer; execute(); }

    @Override protected Boolean doInBackground(Void... voids) { try {
        Socket sock = new Socket();
        sock.connect(new InetSocketAddress("8.8.4.4", 53), 2000);
        sock.close();
        return true;
    } catch (IOException e) { return false; } }

    @Override protected void onPostExecute(Boolean internet) { mConsumer.accept(internet); }}
