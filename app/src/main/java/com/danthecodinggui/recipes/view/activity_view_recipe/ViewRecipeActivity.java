package com.danthecodinggui.recipes.view.activity_view_recipe;

import android.Manifest;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.danthecodinggui.recipes.BR;
import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.databinding.ActivityViewRecipeBinding;
import com.danthecodinggui.recipes.databinding.ActivityViewRecipePhotoBinding;
import com.danthecodinggui.recipes.model.object_models.Recipe;
import com.danthecodinggui.recipes.msc.AnimUtils;
import com.danthecodinggui.recipes.msc.MaterialColours;
import com.danthecodinggui.recipes.msc.PermissionsHandler;
import com.danthecodinggui.recipes.msc.Utility;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.danthecodinggui.recipes.msc.GlobalConstants.IMAGE_TRANSITION_NAME;
import static com.danthecodinggui.recipes.msc.GlobalConstants.RECIPE_DETAIL_BUNDLE;
import static com.danthecodinggui.recipes.msc.GlobalConstants.RECIPE_DETAIL_OBJECT;

/**
 * Display details of a specific recipe
 */
public class ViewRecipeActivity extends AppCompatActivity
        implements AppBarLayout.OnOffsetChangedListener {

    //TODO duplicate static value, find way to push into 1 class
    //Permission request codes
    private static final int REQ_CODE_READ_EXTERNAL = 211;

    //Instance State tags
    private static final String STATE_MATERIAL_COLOUR = "STATE_MATERIAL_COLOUR";

    private String imageTransitionName;

    private int randIngredientsCol = -1;

    ActivityViewRecipeBinding binding;
    ActivityViewRecipePhotoBinding bindingPhoto;

    private RecipePagerAdapter pagerAdapter;

    private Recipe recipe;

    private boolean closingAnimating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        recipe = extras.getBundle(RECIPE_DETAIL_BUNDLE).getParcelable(RECIPE_DETAIL_OBJECT);

        if (recipe.hasPhoto()) {
            imageTransitionName = extras.getString(IMAGE_TRANSITION_NAME);

            int response = PermissionsHandler.AskForPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE, REQ_CODE_READ_EXTERNAL);

            switch (response) {
                case PermissionsHandler.PERMISSION_GRANTED:
                    SetupPhotoLayout();
                    break;
                case PermissionsHandler.PERMISSION_DENIED:
                    SetupNoPhotoLayout();
                    break;
            }
        }
        else {
            if (savedInstanceState != null)
                randIngredientsCol = savedInstanceState.getInt(STATE_MATERIAL_COLOUR);
            SetupNoPhotoLayout();
        }

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(STATE_MATERIAL_COLOUR, randIngredientsCol);
    }

    /**
     * Setup layout with CollapsingToolbarLayout and all it's components, including the relevant
     *  data bindings
     */
    private void SetupPhotoLayout() {

        getWindow().setEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.view_activity_photo_enter));
        getWindow().setReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.view_activity_photo_return));

        bindingPhoto = DataBindingUtil.setContentView(this, R.layout.activity_view_recipe_photo);
        bindingPhoto.setRecipe(recipe);
        bindingPhoto.setVariable(BR.imageLoadedCallback, new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                SetScrimColour(bindingPhoto.ablViewRecipe, resource);
                startPostponedEnterTransition();
                return false;
            }
        });

        //Resize CollapsingToolbarLayout title text size based on title length
        int titleSize = recipe.getTitle().length();
        //Need to cast as CollapsingToolbarLayout is a library implementation
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)bindingPhoto.ctlVwRecipe;
        if (titleSize <= 5)
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.CollapsingToolbarTitleMax);
        else if (titleSize <= 10)
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.CollapsingToolbarTitleLarge);
        else if (titleSize <= 20)
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.CollapsingToolbarTitleMedium);
        else if (titleSize <= 30)
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.CollapsingToolbarTitleSmall);
        else
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.CollapsingToolbarTitleMin);

        if (Utility.atLeastLollipop()) {
            //Set the shared elements transition name
            postponeEnterTransition();
            bindingPhoto.ivwToolbarPreview.setTransitionName(imageTransitionName);

            //Set status bar colour
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);

            window.getSharedElementReturnTransition().setStartDelay(200);
        }

        bindingPhoto.ablViewRecipe.addOnOffsetChangedListener(this);

        setSupportActionBar(bindingPhoto.tbarVwRecipe);

        SetupTabLayout(bindingPhoto.tlyViewRecipe, bindingPhoto.vprViewRecipe);
    }

    /**
     * Setup layout with standard toolbar and tablayout, including the relevant data bindings
     */
    private void SetupNoPhotoLayout() {

        getWindow().setEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.view_activity_enter));
        getWindow().setReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.view_activity_return));

        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_recipe);

        if (recipe.hasExtendedInfo()) {
            binding.clyVwOptionals.setVisibility(View.VISIBLE);
        }

        binding.setRecipe(recipe);

        setSupportActionBar(binding.tbarVwRecipe);

        //Set random colour of layout
        SetLayoutColour();

        SetupTabLayout(binding.tlyViewRecipe, binding.vprViewRecipe);
    }

    /**
     * In the event of no stored photo for the recipe, generate a random material design colour and
     * apply it to the toolbar
     */
    private void SetLayoutColour() {
        if (randIngredientsCol == -1)
            randIngredientsCol = MaterialColours.nextColour();

        binding.tbarVwRecipe.setBackgroundColor(randIngredientsCol);
        binding.clyVwOptionals.setBackgroundColor(randIngredientsCol);
        binding.tlyViewRecipe.setBackgroundColor(randIngredientsCol);
        binding.vwFragBackground.setBackgroundColor(randIngredientsCol);

        if (Utility.atLeastLollipop()) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(randIngredientsCol);
        }
    }

    /**
     * Generates one colour from the recipe preview photo to use as toolbar colour
     */
    private void SetScrimColour(final AppBarLayout appBar, Drawable res) {
        BitmapDrawable drawable = (BitmapDrawable) res;
        Bitmap bitmap = drawable.getBitmap();

        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                int mutedColor = palette.getMutedColor(R.attr.colorPrimary);
                appBar.setBackgroundColor(mutedColor);
            }
        });
    }

    /**
     * Initialise ingredient/method tabs
     */
    private void SetupTabLayout(TabLayout tabLayout, final ViewPager viewPager) {
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        List<String> tabTitles = new ArrayList<>(Arrays.asList(
                getString(R.string.ingredients),
                getString(R.string.method)));

        tabLayout.setupWithViewPager(viewPager);

        pagerAdapter = new RecipePagerAdapter(getSupportFragmentManager(),
                tabLayout.getTabCount(), tabTitles, recipe.getRecipeId());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                Drawable icon = tab.getIcon();

                if (tab.getPosition() == 0)
                    AnimUtils.animateVectorDrawable(icon);
                else if (tab.getPosition() == 1)
                    AnimUtils.animateVectorDrawable(icon);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_ingredients);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_method);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (bindingPhoto != null) {
            //Animate the appbar layout to expand before exiting
            bindingPhoto.ablViewRecipe.setExpanded(true, true);
            closingAnimating = true;
        }
        else
            supportFinishAfterTransition();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        //Fades the scrim colour into the photo as appbar scrolls up/down
        //Fully extended = normal image, Fully retracted = image partially obscured by colour
        int toolBarHeight = bindingPhoto.tbarVwRecipe.getMeasuredHeight();
        int appBarHeight = appBarLayout.getMeasuredHeight();
        float transitionSpace = (float)appBarHeight - toolBarHeight;

        Float f = ((transitionSpace + verticalOffset) / transitionSpace) * 255;
        bindingPhoto.ivwToolbarPreview.setImageAlpha(Math.round(f));

        //When AppBarLayout expansion fully animated, THEN the activity can close
        if (closingAnimating && verticalOffset == 0)
            supportFinishAfterTransition();

        float adjustedF = (f - 102) / 0.255f - 200;
        if (adjustedF < 0)
            adjustedF = 0;

        Log.d("temp", "adjustedF = " + (400 - adjustedF));

        bindingPhoto.txtVwDuration.setTranslationX(400 - adjustedF);
        bindingPhoto.txtVwKcal.setTranslationX(400 - adjustedF);
    }
}
