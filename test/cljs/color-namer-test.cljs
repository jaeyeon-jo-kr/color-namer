(ns color-namer-test
  (:require [color-namer :as sut]
            [cljs.test :as t :include-macros true]))

(use-fixtures :each
  {:after rtl/cleanup})




