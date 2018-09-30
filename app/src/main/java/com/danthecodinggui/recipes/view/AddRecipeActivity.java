package com.danthecodinggui.recipes.view;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.databinding.DataBindingUtil;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.databinding.ActivityAddRecipeBinding;
import com.danthecodinggui.recipes.databinding.AddIngredientItemBinding;
import com.danthecodinggui.recipes.model.object_models.Ingredient;
import com.danthecodinggui.recipes.model.object_models.MethodStep;
import com.danthecodinggui.recipes.msc.StringUtils;
import com.danthecodinggui.recipes.msc.Utility;

import java.util.ArrayList;
import java.util.List;


/**
 * Provides functionality to add recipes
 */
public class AddRecipeActivity extends AppCompatActivity implements
        CaloriesPickerFragment.onCaloriesSetListener,
        DurationPickerFragment.onDurationSetListener{

    ActivityAddRecipeBinding binding;

    private static final String FRAG_TAG_TIME = "FRAG_TAG_TIME";
    private static final String FRAG_TAG_KCAL = "FRAG_TAG_KCAL";

    private static final String DURATION = "DURATION";
    private static final String KCAL = "KCAL";

    private boolean openMenuOpen = false;

    private int recipeDuration;
    private int recipeKcalPerPerson;

    private boolean ingredientsExpanded = false;
    private boolean methodExpanded = false;

    private BottomSheetBehavior photoSheetBehaviour;
    private boolean photoSheetExpanded = false;

    private List<Ingredient> newIngredients;
    private IngredientsAddAdapter ingAdapter;

    private List<MethodStep> newMethod;

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
                timeFrag.SetDurationListener(this);
            if (kcalFrag != null)
                kcalFrag.SetCaloriesListener(this);
        }

        SetupLayoutAnimator();

        //TODO: remove later and replace with data binding variable
        Glide.with(this)
                .load(R.drawable.sample_image)
                .into(binding.imvAddImage);

        newIngredients = new ArrayList<>();
        newMethod = new ArrayList<>();

        //TODO move to dedicated class
        binding.rvwNewIngredients.setLayoutManager(new LinearLayoutManager(this));
        ingAdapter = new IngredientsAddAdapter();
        binding.rvwNewIngredients.setAdapter(ingAdapter);

        photoSheetBehaviour = BottomSheetBehavior.from(binding.includeImageSheet.addImage);
        photoSheetBehaviour.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED)
                    photoSheetExpanded = true;
                else if (newState == BottomSheetBehavior.STATE_COLLAPSED)
                    photoSheetExpanded = false;
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

        newIngredients.add(new Ingredient("a"));
//        newIngredients.add(new Ingredient("b"));
//        newIngredients.add(new Ingredient("c"));
//        newIngredients.add(new Ingredient("d"));
//        newIngredients.add(new Ingredient("e"));
//        newIngredients.add(new Ingredient("f"));
//        newIngredients.add(new Ingredient("g"));
//        newIngredients.add(new Ingredient("h"));
//        newIngredients.add(new Ingredient("i"));
//        newIngredients.add(new Ingredient("j"));
//        newIngredients.add(new Ingredient("k"));
//        newIngredients.add(new Ingredient("a"));
//        newIngredients.add(new Ingredient("b"));
//        newIngredients.add(new Ingredient("c"));
//        newIngredients.add(new Ingredient("d"));
//        newIngredients.add(new Ingredient("e"));
//        newIngredients.add(new Ingredient("f"));
//        newIngredients.add(new Ingredient("g"));
//        newIngredients.add(new Ingredient("h"));
//        newIngredients.add(new Ingredient("i"));
//        newIngredients.add(new Ingredient("j"));
//        newIngredients.add(new Ingredient("k"));
        ingAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(DURATION, recipeDuration);
        outState.putInt(KCAL, recipeKcalPerPerson);
    }

    @Override
    public void onBackPressed() {
        if (ingredientsExpanded)
            RetractIngredientsCard();
        else if (methodExpanded)
            ToggleMethodCard();
        else if (photoSheetExpanded)
            photoSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        else
            super.onBackPressed();
    }

    @Override public boolean dispatchTouchEvent(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (photoSheetBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {

                Rect outRect = new Rect();
                binding.includeImageSheet.addImage.getGlobalVisibleRect(outRect);

                if(!outRect.contains((int)event.getRawX(), (int)event.getRawY()))
                    photoSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }

        return super.dispatchTouchEvent(event);
    }

    private void ToggleMethodCard() {

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

        binding.llyAddContainer.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        binding.ctlyIngredientsContainer.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        binding.ctlyMethodContainer.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
    }

    /**
     * Animates fab menu to open/close
     * @param view The menu fab view
     */
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

    /**
     * Animates individual fab item both in/out of view
     * @param menuItem The menu fab view
     */
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
//        if (binding.imvImageContainer.getVisibility() == View.GONE) {
//            binding.imvImageContainer.setVisibility(View.VISIBLE);
//            binding.tbarAdd.setElevation(Utility.dpToPx(this, 10));
//            binding.clyAddTbar.setElevation(10);
//        }

        photoSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    /**
     * Opens dialog for user to enter number of kcal
     * @param view
     */
    public void AddKcal(View view) {

        CaloriesPickerFragment kcalFrag = new CaloriesPickerFragment();
        kcalFrag.SetCaloriesListener(this);
        kcalFrag.show(getFragmentManager(), FRAG_TAG_KCAL);
    }

    /**
     * Opens dialog for user to enter duration to make recipe
     * @param view
     */
    public void AddTime(View view) {

        DurationPickerFragment timeFrag = new DurationPickerFragment();
        timeFrag.SetDurationListener(this);

        timeFrag.show(getFragmentManager(), FRAG_TAG_TIME);
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

    public void RemoveImage(View view) {
        binding.imvImageContainer.setVisibility(View.GONE);
        binding.tbarAdd.setElevation(Utility.dpToPx(this, 10));
        binding.clyAddTbar.setElevation(Utility.dpToPx(this, 10));
    }

    /**
     * Resets duration value and removes view
     * @param view
     */
    public void RemoveDuration(View view) {
        binding.butTime.setVisibility(View.GONE);
        recipeDuration = 0;
    }
    /**
     * Resets kcal value and removes view
     * @param view
     */
    public void RemoveKcal(View view) {
        binding.butKcal.setVisibility(View.GONE);
        recipeKcalPerPerson = 0;
    }

    private void ExpandIngredientsCard() {

        binding.imvNoIngredients.setVisibility(View.GONE);
        binding.rvwNewIngredients.setVisibility(View.VISIBLE);

//        int rvwSize = binding.cdlyAddRoot.getMeasuredHeight() - Utility.dpToPx(this, 150) - binding.txtIngredientsTitle.getMeasuredHeight() - binding.etxtAddIngredient.getMeasuredHeight();
//
//        binding.rvwNewIngredients.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                rvwSize));

        binding.crdvIngredients.setElevation(Utility.dpToPx(this, 10));
        binding.crdvMethod.setVisibility(View.GONE);
        binding.llyToolbarContainer.setVisibility(View.GONE);
        binding.spcAdd.setVisibility(View.GONE);

        binding.etxtAddIngredient.setVisibility(View.VISIBLE);
        binding.butAddIngredient.setVisibility(View.VISIBLE);

        binding.fabAddMenu.setVisibility(View.INVISIBLE);
        if (openMenuOpen)
            AnimateFabMenu(binding.fabAddMenu);

        ingredientsExpanded = true;
    }

    private void RetractIngredientsCard() {

        if (newIngredients.isEmpty()) {
            binding.imvNoIngredients.setVisibility(View.VISIBLE);
            binding.rvwNewIngredients.setVisibility(View.GONE);
        }

        binding.crdvIngredients.setElevation(Utility.dpToPx(this, 3));
        binding.llyToolbarContainer.setVisibility(View.VISIBLE);
        binding.crdvMethod.setVisibility(View.VISIBLE);
        binding.spcAdd.setVisibility(View.VISIBLE);

        binding.etxtAddIngredient.setVisibility(View.GONE);
        binding.butAddIngredient.setVisibility(View.GONE);

        binding.fabAddMenu.setVisibility(View.VISIBLE);

        ingredientsExpanded = false;
    }

    public void ViewIngredients(View view) {
        if (!ingredientsExpanded)
            ExpandIngredientsCard();
    }

    public void ViewMethod(View view) {
        Toast.makeText(this, "View Method", Toast.LENGTH_SHORT).show();
    }

    public void AddIngredient(View view) {

        String ingredientName = binding.etxtAddIngredient.getText().toString();

        if (ingredientName.equals(""))
            return;

        Ingredient temp = new Ingredient(ingredientName);
        newIngredients.add(temp);
        ingAdapter.notifyItemInserted(newIngredients.size() - 1);

        binding.etxtAddIngredient.setText("");
    }

    //TODO: consolidate adapters for this and view ingredients (are basically the same)
    class IngredientsAddAdapter extends RecyclerView.Adapter<IngredientsAddAdapter.IngredientViewHolder> {

        @Override
        public IngredientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new IngredientViewHolder(AddIngredientItemBinding.inflate(inflater, parent, false));
        }

        @Override
        public void onBindViewHolder(IngredientViewHolder holder, int position) {
            Ingredient ingredient = newIngredients.get(position);
            holder.bind(ingredient);
        }

        @Override
        public int getItemCount() {
            if (newIngredients == null)
                return 0;
            return newIngredients.size();
        }

        class IngredientViewHolder extends RecyclerView.ViewHolder {

            AddIngredientItemBinding binding;

            IngredientViewHolder(AddIngredientItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public void bind(Ingredient item) {
                binding.setIngredient(item);
                binding.executePendingBindings();
            }
        }
    }
}
