package cu.ij.jotalab.mediapro;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import cu.ij.jotalab.mediapro.database.DatabaseController;
import cu.ij.jotalab.mediapro.pojo.Catalog;


public class DetailActivity extends AppCompatActivity {

    List itm;
    TextView title;
    TextView price;
    TextView year;
    TextView desc;
    TextView weigh;
    ImageView logo;
    Button btn_back;
    TextView category;
    public final static int REQUEST_CODE = 30101;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_detail);
        setSupportActionBar(toolbar);
        itm = new ArrayList();
        logo = (ImageView) findViewById(R.id.info_logo);
        title = (TextView) findViewById(R.id.info_title);
        price = (TextView) findViewById(R.id.info_price);
        year = (TextView) findViewById(R.id.info_year);
        weigh = (TextView) findViewById(R.id.info_byte);
        desc = (TextView) findViewById(R.id.info_desc);
        btn_back = (Button) findViewById(R.id.button_back);
        category = (TextView) findViewById(R.id.infoCategory);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent pos = getIntent();
        String id_game = pos.getStringExtra("id_game");
        String[] _id_game = {new String(id_game)};

        DatabaseController controller = new DatabaseController(this,"/");
        controller.open();
        Cursor cursor_catalog = controller.selectCatalogTableById(_id_game);

        while (cursor_catalog.moveToNext()) {
            byte[] logo = cursor_catalog.getBlob(7);
            itm.add(new Catalog(cursor_catalog.getString(0), cursor_catalog.getString(2), cursor_catalog.getString(3),
                    cursor_catalog.getString(4),cursor_catalog.getString(5), cursor_catalog.getString(6), logo,
                    cursor_catalog.getString(8),cursor_catalog.getString(9)));
        }

        final Catalog itemCatalog = (Catalog) itm.get(0);
        final Bitmap bm_img = BitmapFactory.decodeByteArray(itemCatalog.getImage(),0,itemCatalog.getImage().length);
        logo.setImageBitmap(bm_img);
        title.setText(itemCatalog.getName());
        price.setText(itemCatalog.getPrice());
        year.setText(itemCatalog.getYear());
        weigh.setText(itemCatalog.getWeigh());
        desc.setText(itemCatalog.getDescription());
        checkDrawOverlayPermission();
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDialog(DetailActivity.this,bm_img);
                Toast.makeText(DetailActivity.this, "TOCAR LA IMAGEN PARA SALIR", Toast.LENGTH_LONG).show();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToMain = new Intent(DetailActivity.this,DrawerMainActivity.class);
                startActivity(goToMain);
            }
        });
    }

    //check de permission for to show overlay dialogs
    public void checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(DetailActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        }
    }

    //to show de Logo announce in a Dialog
    private void showDialog(Context context, Bitmap full_logo) {
        final WindowManager manager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        layoutParams.gravity = Gravity.CENTER;
        layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.windowAnimations = android.R.style.Animation_Dialog;

        final View view = View.inflate(context.getApplicationContext(),R.layout.full_logo_layout, null);
        ImageView iv_full_logo = (ImageView) view.findViewById(R.id.iv_full_logo);
        iv_full_logo.setImageBitmap(full_logo);

        iv_full_logo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                manager.removeView(view);
                return false;
            }
        });
        manager.addView(view, layoutParams);
    }


}
