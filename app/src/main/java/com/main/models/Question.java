package com.main.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Question implements Parcelable {
    public String cauhoi;
    public String caseA, caseB, caseC;
    public int trueCase;

    public Question() {
    }

    public Question(String cauhoi, String caseA, String caseB, String caseC, int trueCase) {
        this.cauhoi = cauhoi;
        this.caseA = caseA;
        this.caseB = caseB;
        this.caseC = caseC;
        this.trueCase = trueCase;
    }

    protected Question(Parcel in) {
        cauhoi = in.readString();
        caseA = in.readString();
        caseB = in.readString();
        caseC = in.readString();
        trueCase = in.readInt();
    }

    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {

        dest.writeString(cauhoi);
        dest.writeString(caseA);
        dest.writeString(caseB);
        dest.writeString(caseC);
        dest.writeInt(trueCase);
    }


    public String getCauhoi() {
        return cauhoi;
    }

    public void setCauhoi(String cauhoi) {
        this.cauhoi = cauhoi;
    }

    public String getCaseA() {
        return caseA;
    }

    public void setCaseA(String caseA) {
        this.caseA = caseA;
    }

    public String getCaseB() {
        return caseB;
    }

    public void setCaseB(String caseB) {
        this.caseB = caseB;
    }

    public String getCaseC() {
        return caseC;
    }

    public void setCaseC(String caseC) {
        this.caseC = caseC;
    }

    public int getTrueCase() {
        return trueCase;
    }

    public void setTrueCase(int trueCase) {
        this.trueCase = trueCase;
    }
}
