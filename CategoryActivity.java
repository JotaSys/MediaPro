package cu.ij.jotalab.mediapro;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cu.ij.jotalab.mediapro.AsyncTasks.SyncDatabase;
import cu.ij.jotalab.mediapro.adapters.RecyclerCategoryAdapter;
import cu.ij.jotalab.mediapro.database.DatabaseController;
import cu.ij.jotalab.mediapro.explorer.ExplorerActivity;
import cu.ij.jotalab.mediapro.pojo.Catalog;
import cu.ij.jotalab.mediapro.pojo.Category;
import dmax.dialog.SpotsDialog;

public class CategoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recycler_category;
    private RecyclerCategoryAdapter adapter_recycler;
    private RecyclerView.LayoutManager manager_layout;
    List items;
    DatabaseController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_catego);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_catego);
        navigationView.setNavigationItemSelectedListener(this);

        recycler_category = (RecyclerView) findViewById(R.id.recycler_category);
        recycler_category.setHasFixedSize(true);
        manager_layout = new LinearLayoutManager(this);
        recycler_category.setLayoutManager(manager_layout);

        controller = new DatabaseController(this,"/");
        controller.open();

        Intent intentCatego = getIntent();
        final String category = intentCatego.getStringExtra("category");
        toolbar.setTitle(category);

        String[] catego_arr = {new String(category)};
        Cursor cursorCatego = controller.selectCatalogTableByCategory(catego_arr);
        items = new ArrayList();

        while (cursorCatego.moveToNext()) {
            items.add(new Catalog(cursorCatego.getString(0), cursorCatego.getString(2), cursorCatego.getString(3),
                    cursorCatego.getString(4),cursorCatego.getString(5), cursorCatego.getString(6),cursorCatego.getBlob(7),
                    cursorCatego.getString(8),cursorCatego.getString(9)));
        }
        controller.getHelper().close();
        adapter_recycler = new RecyclerCategoryAdapter(items);
        adapter_recycler.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go_to_detail = new Intent(CategoryActivity.this,DetailActivity.class);
                String idGame = ((TextView)v.findViewById(R.id.by_category_id)).getText().toString();
                go_to_detail.putExtra("id_game",idGame);
                startActivity(go_to_detail);
            }
        });
        recycler_category.setAdapter(adapter_recycler);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_catego);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent goToPortada = new Intent(CategoryActivity.this, DrawerMainActivity.class);
            startActivity(goToPortada);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_terms) {
            Intent goToTerms = new Intent(CategoryActivity.this,TermsConditionsActivity.class);
            goToTerms.putExtra("mainmenu", "terms");
            startActivity(goToTerms);
        }
        else if (id == R.id.action_help) {
            Intent goToHelp = new Intent(CategoryActivity.this,TermsConditionsActivity.class);
            goToHelp.putExtra("mainmenu", "help");
            startActivity(goToHelp);
        }
        else if (id == R.id.action_about) {
            Intent goToAbout = new Intent(CategoryActivity.this,TermsConditionsActivity.class);
            goToAbout.putExtra("mainmenu", "about");
            startActivity(goToAbout);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent goToMain = new Intent(CategoryActivity.this,DrawerMainActivity.class);
            startActivity(goToMain);

        } else if (id == R.id.nav_sync) {

            final SpotsDialog loading = new SpotsDialog(CategoryActivity.this, "sync Catálogo");
            loading.show();

            final Handler handler = new Handler();
            final SyncDatabase syncDatabase= new SyncDatabase(CategoryActivity.this);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                syncDatabase.synchronizedDataBaseFromService();
                                if (syncDatabase.getResult() !="")
                                {
                                    Toast.makeText(CategoryActivity.this, syncDatabase.getResult(), Toast.LENGTH_SHORT).show();
                                    loading.dismiss();
                                }
                                else
                                {
                                    Intent goToMain = new Intent(CategoryActivity.this,DrawerMainActivity.class);
                                    startActivity(goToMain);
                                }
                            }
                        });
                    }
                    catch (InterruptedException ie)
                    {ie.printStackTrace();}
                }
            }).start();

        }else if (id == R.id.nav_copy) {
            controller.open();
            try {
                controller.getHelper().duplicateDataBaseFromFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            controller.getHelper().close();

        } else if (id == R.id.nav_load) {
            Intent openSD = new Intent(CategoryActivity.this, ExplorerActivity.class);
            startActivity(openSD);

        } else if (id == R.id.nav_call) {

            try {
                Uri numero = Uri.parse("tel:" + "53147931");
                Intent intent = new Intent(Intent.ACTION_CALL, numero);
                if (ActivityCompat.checkSelfPermission(CategoryActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "NO SE PUDO REALIZAR LA LLAMADA", Toast.LENGTH_SHORT).show();
                    return false;
                }
                startActivity(intent);
            }
            catch (ActivityNotFoundException activityException){
                Log.e("CallEx", "NO SE PUDO REALIZAR LA LLAMADA, VER LOGCAT", activityException);}

        } else if (id == R.id.nav_sms) {
            Toast.makeText(this, "FUNCION NO DISPONIBLE", Toast.LENGTH_SHORT).show();

        }else if (id == R.id.cat_action_adventure) {
            Intent category_game = new Intent(CategoryActivity.this,CategoryActivity.class);
            category_game.putExtra("category","Accion-Aventura");
            startActivity(category_game);

        } else if (id == R.id.cat_arcade_music_puzzle) {
            Intent category_game = new Intent(CategoryActivity.this,CategoryActivity.class);
            category_game.putExtra("category","Arcade-MÃºsica-Puzzle");
            startActivity(category_game);

        } else if (id == R.id.cat_race) {
            Intent category_game = new Intent(CategoryActivity.this,CategoryActivity.class);
            category_game.putExtra("category","Carrera");
            startActivity(category_game);

        } else if (id == R.id.cat_sport) {
            Intent category_game = new Intent(CategoryActivity.this,CategoryActivity.class);
            category_game.putExtra("category","Deporte");
            startActivity(category_game);

        } else if (id == R.id.cat_strategy) {
            Intent category_game = new Intent(CategoryActivity.this,CategoryActivity.class);
            category_game.putExtra("category","Estrategia");
            startActivity(category_game);

        }else if (id == R.id.cat_graphic_adv) {
            Intent category_game = new Intent(CategoryActivity.this,CategoryActivity.class);
            category_game.putExtra("category","Graphic Adv.");
            startActivity(category_game);

        } else if (id == R.id.cat_horror) {
            Intent category_game = new Intent(CategoryActivity.this,CategoryActivity.class);
            category_game.putExtra("category","Horror");
            startActivity(category_game);

        } else if (id == R.id.cat_kids) {
            Intent category_game = new Intent(CategoryActivity.this,CategoryActivity.class);
            category_game.putExtra("category","Kids");
            startActivity(category_game);

        } else if (id == R.id.cat_figth) {
            Intent category_game = new Intent(CategoryActivity.this,CategoryActivity.class);
            category_game.putExtra("category","Pelea");
            startActivity(category_game);

        } else if (id == R.id.cat_rpg) {
            Intent category_game = new Intent(CategoryActivity.this,CategoryActivity.class);
            category_game.putExtra("category","RPG");
            startActivity(category_game);

        }else if (id == R.id.cat_sandbox) {
            Intent category_game = new Intent(CategoryActivity.this,CategoryActivity.class);
            category_game.putExtra("category","Sandbox");
            startActivity(category_game);

        } else if (id == R.id.cat_shoter) {
            Intent category_game = new Intent(CategoryActivity.this,CategoryActivity.class);
            category_game.putExtra("category","Shooter");
            startActivity(category_game);

        } else if (id == R.id.cat_simulator) {
            Intent category_game = new Intent(CategoryActivity.this,CategoryActivity.class);
            category_game.putExtra("category","Simulador");
            startActivity(category_game);

        } else if (id == R.id.cat_comming_soon) {
            Intent category_game = new Intent(CategoryActivity.this,CategoryActivity.class);
            category_game.putExtra("category","Proximos estrenos");
            startActivity(category_game);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_catego);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
