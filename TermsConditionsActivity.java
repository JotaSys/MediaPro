package cu.ij.jotalab.mediapro;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TermsConditionsActivity extends AppCompatActivity {

    TextView tv_body;
    String itemValue = "algo";
    TextView tv_title;
    Button btn_back;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_conditions);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        tv_title = (TextView) findViewById(R.id.tv_tcs);
        btn_back = (Button) findViewById(R.id.button_back2);
        tv_body = (TextView) findViewById(R.id.textToScroll);
        Intent itemMenu = getIntent();
        itemValue = itemMenu.getStringExtra("mainmenu");

        if(itemValue.compareTo("terms")==0)
        {
            tv_body.setText(R.string.terms);
            //toolbar.setTitle(R.string.titleTerms);
            tv_title.setText(R.string.titleTerms);
        }

        else if(itemValue.compareTo("about")==0)
        {
            tv_body.setText(R.string.about);
            //toolbar.setTitle(R.string.titleAbout);
            tv_title.setText(R.string.titleAbout);
        }
        else if(itemValue.compareTo("help")==0)
        {
            tv_body.setText(R.string.help);
            //toolbar.setTitle(R.string.titleHelp);
            tv_title.setText(R.string.titleHelp);
        }

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

}
