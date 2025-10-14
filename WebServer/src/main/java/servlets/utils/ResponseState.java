package servlets.utils;

public enum ResponseState {
    SUCCESS,ERROR;

    @Override
    public String toString(){
        return name();
    }
}
