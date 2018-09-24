package com.danthecodinggui.recipes.view;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;

import com.bumptech.glide.Glide;
import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.databinding.ActivityAddRecipeBinding;
import com.danthecodinggui.recipes.msc.StringUtils;
import com.danthecodinggui.recipes.msc.Utility;



public class AddRecipeActivity extends AppCompatActivity implements
        CaloriesPickerFragment.onCaloriesSetListener,
        DurationPickerFragment.onDurationSetListener{

    ActivityAddRecipeBinding binding;

    private static final String FRAG_TAG_TIME = "FRAG_TAG_TIME";
    private static final String FRAG_TAG_KCAL = "FRAG_TAG_KCAL";

    private static final int FRAG_TARGET_TIME = 1;

    private static final String DURATION = "DURATION";
    private static final String KCAL = "KCAL";

    private boolean openMenuOpen = false;

    private int recipeDuration;
    private int recipeKcalPerPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_recipe);

        setSupportActionBar(binding.tbarAdd);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState != null) {
            recipeDuration = savedInstanceState.getInt(DURATION);
            recipeKcalPerPerson = savedInstanceState.getInt(KCAL);
            if (recipeDuration != 0)
                onDurationSet(recipeDuration);
            if (recipeKcalPerPerson != 0)
                onCaloriesSet(recipeKcalPerPerson);

            DurationPickerFragment timeFrag = (DurationPickerFragment) getFragmentManager().findFragmentByTag(FRAG_TAG_TIME);
            CaloriesPickerFragment kcalFrag = (CaloriesPickerFragment) getFragmentManager().findFragmentByTag(FRAG_TAG_KCAL);
            if (timeFrag != null)
                SetDurationFragListener(timeFrag);
            if (kcalFrag != null)
                SetKcalFragListener(kcalFrag);
        }

        SetupLayoutAnimator();

        Glide.with(this)
                .load(R.drawable.sample_image)
                .into(binding.imvAddImage);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(DURATION, recipeDuration);
        outState.putInt(KCAL, recipeKcalPerPerson);
    }

    /**
     * Setup image enter/exit animation in toolbar
     */
    private void SetupLayoutAnimator() {
        ObjectAnimator slideDown = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.slide_down_animator);

        Animator slideUp = AnimatorInflater.loadAnimator(this, R.animator.slide_up_animator);

        LayoutTransition imageSlider = new LayoutTransition();
        imageSlider.setAnimator(LayoutTransition.APPEARING, slideDown);
        imageSlider.setAnimator(LayoutTransition.DISAPPEARING, slideUp);

        binding.llyToolbarContainer.setLayoutTransition(imageSlider);
    }

    public void AnimateFabMenu(View view) {
        if (openMenuOpen) {
            binding.fabAddMenu.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_backwards));
            //TODO change this to list that you iterate through
            AnimateFabItem(binding.fabAddPhoto);
            AnimateFabItem(binding.txtAddPhoto);
            AnimateFabItem(binding.fabAddTime);
            AnimateFabItem(binding.txtAddTime);
            AnimateFabItem(binding.fabAddKcal);
            AnimateFabItem(binding.txtAddKcal);
            openMenuOpen = false;
        }
        else {
            binding.fabAddMenu.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_forwards));
            AnimateFabItem(binding.fabAddPhoto);
            AnimateFabItem(binding.txtAddPhoto);
            AnimateFabItem(binding.fabAddTime);
            AnimateFabItem(binding.txtAddTime);
            AnimateFabItem(binding.fabAddKcal);
            AnimateFabItem(binding.txtAddKcal);
            openMenuOpen = true;
        }
    }

    private void AnimateFabItem(View menuItem) {
        AnimationSet set = new AnimationSet(true);
        Animation rotate;

        float fabMenuXDelta = (binding.fabAddMenu.getX() + binding.fabAddMenu.getWidth() / 2) - (menuItem.getX() + menuItem.getWidth() / 2);
        float fabMenuYDelta = (binding.fabAddMenu.getY() + binding.fabAddMenu.getHeight() / 2) - (menuItem.getY() + menuItem.getHeight() / 2);

        if (openMenuOpen) {
            rotate = new RotateAnimation(0.f, -150.f, fabMenuXDelta + binding.fabAddMenu.getWidth() / 2, fabMenuYDelta + binding.fabAddMenu.getHeight() / 2);

            set.addAnimation(rotate);

            menuItem.setClickable(false);
        }
        else {
            Animation rotateBounce;

            rotate = new RotateAnimation(-150.f, 10.f, fabMenuXDelta + binding.fabAddMenu.getWidth() / 2, fabMenuYDelta + binding.fabAddMenu.getHeight() / 2);
            rotateBounce = new RotateAnimation(0.f, -10.f, fabMenuXDelta + binding.fabAddMenu.getWidth() / 2, fabMenuYDelta + binding.fabAddMenu.getHeight() / 2);
            rotateBounce.setStartOffset(250);
            rotateBounce.setDuration(500);

            set.addAnimation(rotate);
            set.addAnimation(rotateBounce);

            menuItem.setClickable(true);
        }

        set.setDuration(300);
        set.setFillAfter(true);

        menuItem.startAnimation(set);
    }

    public void addPhoto(View view) {
        if (binding.imvAddImage.getVisibility() == View.GONE) {
            binding.imvAddImage.setVisibility(View.VISIBLE);
            binding.tbarAdd.setElevation(Utility.dpToPx(this, 10));
            binding.clyAddTbar.setElevation(10);
        }
        else {
            binding.imvAddImage.setVisibility(View.GONE);
            binding.tbarAdd.setElevation(Utility.dpToPx(this, 10));
            binding.clyAddTbar.setElevation(Utility.dpToPx(this, 10));
        }
    }

    public void AddKcal(View view) {

        CaloriesPickerFragment kcalFrag = new CaloriesPickerFragment();
        SetKcalFragListener(kcalFrag);
        kcalFrag.show(getFragmentManager(), FRAG_TAG_KCAL);
    }

    /**
     * Opens duration picker dialog
     * @param view
     */
    public void AddTime(View view) {

        DurationPickerFragment timeFrag = new DurationPickerFragment();
        SetDurationFragListener(timeFrag);

        timeFrag.show(getFragmentManager(), FRAG_TAG_TIME);
    }

    private void SetDurationFragListener(DurationPickerFragment timeFrag) {
        timeFrag.SetDurationListener(this);
    }

    private void SetKcalFragListener(CaloriesPickerFragment kcalFrag) {
        kcalFrag.SetCaloriesListener(this);
    }

    @Override
    public void onCaloriesSet(int kcal) {
        binding.butKcal.setVisibility(View.VISIBLE);

        String kcalFormatted = getResources().getString(R.string.txt_kcal_per_person, kcal);
        binding.txtKcal.setText(kcalFormatted);

        recipeKcalPerPerson = kcal;
    }

    @Override
    public void onDurationSet(int minutes) {
        binding.butTime.setVisibility(View.VISIBLE);
        binding.txtTime.setText(StringUtils.minsToHourMins(minutes));
        recipeDuration = minutes;
    }
}
