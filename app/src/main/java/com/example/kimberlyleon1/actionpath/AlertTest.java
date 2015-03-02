package com.example.kimberlyleon1.actionpath;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by kimberlyleon1 on 3/2/15.
 */
public class AlertTest extends Activity {


    private Button mainBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert);
        mainBtn = (Button) findViewById(R.id.button);
        mainBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                openAlert(v);
            }
        });
    }



    private void openAlert(View view) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AlertTest.this);


        alertDialogBuilder.setTitle(this.getTitle()+ " decision");

        alertDialogBuilder.setMessage("Are you sure?");
        // set positive button: Yes message
        alertDialogBuilder.setPositiveButton("Respond",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                // go to a new activity of the app
                Intent positveActivity = new Intent(getApplicationContext(),
                        Response.class);
                startActivity(positveActivity);
            }
        });

        // set negative button: No message

        alertDialogBuilder.setNegativeButton("Ignore",new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog,int id) {
                // cancel the alert box and put a Toast to the user
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "You chose a negative answer",
                        Toast.LENGTH_LONG).show();
            }
        });

        // set neutral button: Exit the app message

        alertDialogBuilder.setNeutralButton("Exit the app",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                // exit the app and go to the HOME
                AlertTest.this.finish();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show alert
        alertDialog.show();

    }
}

