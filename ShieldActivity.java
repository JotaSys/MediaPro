package cu.ij.jotalab.mediapro;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import cu.ij.jotalab.mediapro.AsyncTasks.SyncCatalog;
import cu.ij.jotalab.mediapro.AsyncTasks.SyncCatalogCategory;
import cu.ij.jotalab.mediapro.AsyncTasks.SyncCategory;
import cu.ij.jotalab.mediapro.database.DatabaseController;
import cu.ij.jotalab.mediapro.explorer.ExplorerActivity;
import dmax.dialog.SpotsDialog;

import static android.os.Build.VERSION.SDK_INT;

public class ShieldActivity extends AppCompatActivity {

    Button loadSD;
    Button sync;
    String path;
    DatabaseController controller;
    private static final int CODIGO_SOLICITUD_PERMISO = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shield);
        //Toast.makeText(this, Environment.getExternalStorageDirectory().getPath(), Toast.LENGTH_SHORT).show();
        loadSD = (Button) findViewById(R.id.load);
        sync = (Button) findViewById(R.id.sync);
        solicitarPermiso();

        controller = new DatabaseController(this,"/");
        if(controller.getHelper().checkDataBaseFile())
        {
            controller.open();
            Cursor curSequence = controller.getSQLSequence();
            if(curSequence.moveToPosition(0) && curSequence.getString(0).equals("catalog")) {
                if (curSequence.moveToPosition(1) && curSequence.getString(0).equals("category")) {
                    if (curSequence.moveToPosition(2) && curSequence.getString(0).equals("catalogcategory")) {

                        controller.getHelper().close();
                        Intent load = new Intent(ShieldActivity.this, DrawerMainActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("path", path);
                        load.putExtras(bundle);
                        startActivity(load);
                    } else {
                        Toast.makeText(this, "El archivo de datos es ilegible o esta dañado", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(this, "El archivo de datos es ilegible o esta dañado", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(this, "El archivo de datos es ilegible o esta dañado", Toast.LENGTH_SHORT).show();
            }
        }
        else
            Toast.makeText(this, "No existen origenes de Datos", Toast.LENGTH_LONG).show();

        loadSD.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openSD = new Intent(ShieldActivity.this, ExplorerActivity.class);
                startActivity(openSD);
            }
        });

        sync.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                final SpotsDialog loading = new SpotsDialog(ShieldActivity.this, "sync Catálogo");
                loading.show();

                final Handler handler = new Handler();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(synchronizedDataBaseFromService())
                                    {
                                        Intent goToDrawerMA = new Intent(ShieldActivity.this,DrawerMainActivity.class);
                                        startActivity(goToDrawerMA);
                                    }
                                    else loading.dismiss();
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }


    private void solicitarPermiso(){

        String[] permisosStorage={Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if(SDK_INT >= Build.VERSION_CODES.M){         //Verificamos si la version de android del dispositivo es mayor
            requestPermissions(permisosStorage,CODIGO_SOLICITUD_PERMISO);  //o igual a MarshMallow
        }
    }

    public boolean synchronizedDataBaseFromService()
    {
        //this is for async catalog
        SyncCatalog syncCatalog = new SyncCatalog(this);
        SyncCategory syncCategory = new SyncCategory(this);
        SyncCatalogCategory syncCatalogCategory = new SyncCatalogCategory(this);
        //syncCatalog.updater();
        //syncCategory.updater();
        //syncCatalogCategory.updater();
        if (syncCatalog.updater()==true && syncCategory.updater()==true && syncCatalogCategory.updater() ==true) {

            Toast.makeText(this, "Conexión establesida", Toast.LENGTH_SHORT).show();
            final SQLiteDatabase dbexe = controller.getHelper().getWritableDatabase();

            for (int i=0;i<syncCatalog.catalogList.size();i++)
            {
                ContentValues catalogCV = new ContentValues();
                catalogCV.put("name",syncCatalog.catalogList.get(i).getName());
                catalogCV.put("idweb",syncCatalog.catalogList.get(i).getId());
                catalogCV.put("description",syncCatalog.catalogList.get(i).getDescription());
                catalogCV.put("year",syncCatalog.catalogList.get(i).getYear());
                catalogCV.put("weigh",syncCatalog.catalogList.get(i).getWeigh());
                catalogCV.put("price",syncCatalog.catalogList.get(i).getPrice());
                catalogCV.put("image",syncCatalog.catalogList.get(i).getImage());
                catalogCV.put("created",syncCatalog.catalogList.get(i).getCreated());
                catalogCV.put("updated",syncCatalog.catalogList.get(i).getUpdated());
                dbexe.insert("catalog",null,catalogCV);
            }
            //en async catalog

            //this is for async category
            for (int i=0;i<syncCategory.categoryList.size();i++)
            {
                ContentValues categoryCV = new ContentValues();
                categoryCV.put("name",syncCategory.categoryList.get(i).getName());
                categoryCV.put("idweb",syncCategory.categoryList.get(i).getId());
                categoryCV.put("description",syncCategory.categoryList.get(i).getDescription());
                categoryCV.put("created",syncCategory.categoryList.get(i).getCreated());
                categoryCV.put("updated",syncCategory.categoryList.get(i).getUpdated());
                dbexe.insert("category",null,categoryCV);
            }
            //en async category

            //this is for async catalog_category
            for (int i=0;i<syncCatalogCategory.catalog_categoryList.size();i++)
            {
                ContentValues catalog_categoryCV = new ContentValues();
                catalog_categoryCV.put("catalogID",syncCatalogCategory.catalog_categoryList.get(i).getCatalogID());
                catalog_categoryCV.put("categoryID",syncCatalogCategory.catalog_categoryList.get(i).getCategoryID());
                dbexe.insert("catalogcategory",null,catalog_categoryCV);
            }
            //en async catalog_category

            dbexe.close();
            return true;
        }
        else Toast.makeText(this, "No es posible establecer la conexion con el servidor", Toast.LENGTH_SHORT).show();

        return false;
    }

    @Override
    public void onStop() {
        super.onStop();
        finish();
    }




}


