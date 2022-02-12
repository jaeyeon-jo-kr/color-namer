(ns color-namer.router)

(def routes
  ["/" {""         :home
        "login"    :login
        "logout"   :logout
        "register" :register
        "settings" :settings
        "editor/"  {[:slug] :editor}
        "article/" {[:slug] :article}
        "profile/" {[:user-id]
                    {""           :profile
                     "/favorites" :favorited}}}])