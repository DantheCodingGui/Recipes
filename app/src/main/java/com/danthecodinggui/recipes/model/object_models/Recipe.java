package com.danthecodinggui.recipes.model.object_models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * HomeActivity RecyclerView model
 */
public class Recipe implements Parcelable {

    //Primary key in db
    private long recipePk;

    private String title;
    private int ingredientsNo;
    private int stepsNo;

    //Can be a URL of a filepath
    private String imagePath;

    private int calories;
    private int timeInMins;

    private Recipe(long recipePk, String title, Integer calories, int timeInMins, String imagefilePath) {
        this.recipePk = recipePk;
        this.title = title;
        this.calories = calories;
        this.timeInMins = timeInMins;
        this.imagePath = imagefilePath;
    }

    /**
     * States whether or not recipe record includes time and/or Calorie information
     */
    public boolean hasExtendedInfo() {
        return calories != 0 || timeInMins != 0;
    }

    /**
     * States whether or not recipe record includes an attached photo or the completed dish
     */
    public boolean hasPhoto() {
        return imagePath != null;
    }

    public long getRecipeId() {
        return recipePk;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public int getIngredientsNo() {
        return ingredientsNo;
    }
    public void setIngredientsNo(int ingredientsNo) {
        this.ingredientsNo = ingredientsNo;
    }

    public int getStepsNo() {
        return stepsNo;
    }
    public void setStepsNo(int stepsNo) {
        this.stepsNo = stepsNo;
    }

    public String getImagePath() {
        return imagePath;
    }
    public void setImagePath(String newPath) {
        imagePath = newPath;
    }

    public int getCalories() {
        return calories;
    }

    public int getTimeInMins() {
        return timeInMins;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(recipePk);
        parcel.writeString(title);
        parcel.writeInt(ingredientsNo);
        parcel.writeInt(stepsNo);
        parcel.writeString(imagePath);
        parcel.writeInt(calories);
        parcel.writeInt(timeInMins);
    }
    //Parcel-based constructor
    public Recipe(Parcel parcel) {
        this.recipePk = parcel.readLong();
        this.title = parcel.readString();
        this.ingredientsNo = parcel.readInt();
        this.stepsNo = parcel.readInt();
        this.imagePath = parcel.readString();
        this.calories = parcel.readInt();
        this.timeInMins = parcel.readInt();
    }

    //Generates instances of Parcelable class from a Parcel
    public static final Parcelable.Creator<Recipe> CREATOR = new Parcelable.Creator<Recipe>() {

        @Override
        public Recipe createFromParcel(Parcel parcel) {
            return new Recipe(parcel);
        }

        @Override
        public Recipe[] newArray(int size) {
            //Used when parcelable gets a list of ParcelableObjects
            return new Recipe[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Recipe))
            return false;

        Recipe ob = (Recipe) obj;
        return ob.recipePk == recipePk &&
                ob.getTitle().equals(title) &&
                ob.getCalories() == calories &&
                ob.getTimeInMins() == timeInMins &&
                (imagePath == null ? ob.getImagePath() == null : imagePath.equals(ob.getImagePath())) &&
                ob.getIngredientsNo() == ingredientsNo &&
                ob.getStepsNo() == stepsNo;
    }

    /**
     * Builds recipe objects, required due to large amount of constructor params and the fact that
     * some are optional
     */
    public static class RecipeBuilder {
        private long nestedRecipePk;

        private String nestedTitle;

        private String nestedImageFilePath;

        private int nestedCalories;
        private int nestedTimeInMins;

        public RecipeBuilder(long recipePk, String title) {
            nestedRecipePk = recipePk;
            nestedTitle = title;
        }

        public RecipeBuilder calories(int calories) {
            nestedCalories = calories;
            return this;
        }
        public RecipeBuilder timeInMins(int timeInMins) {
            nestedTimeInMins = timeInMins;
            return this;
        }
        public RecipeBuilder imageFilePath(String imageFilePath) {
            nestedImageFilePath = imageFilePath;
            return this;
        }

        public Recipe build() {
            return new Recipe(
                    nestedRecipePk,
                    nestedTitle,
                    nestedCalories,
                    nestedTimeInMins,
                    nestedImageFilePath
            );
        }
    }
}
