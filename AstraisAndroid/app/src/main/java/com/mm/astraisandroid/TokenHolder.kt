package com.mm.astraisandroid

object TokenHolder {

    private var accessToken : String = ""
    private var refreshToken : String = ""

    public fun setRefreshToken(refresh : String){
        refreshToken = refresh

    }
    public fun setAccessToken(access : String){
        accessToken = access
    }

    public fun getRefreshToken() : String{
        return refreshToken
    }
    public fun getAccessToken() : String{
        return accessToken
    }
}