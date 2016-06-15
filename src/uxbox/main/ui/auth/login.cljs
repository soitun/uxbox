;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2016 Andrey Antukh <niwi@niwi.nz>

(ns uxbox.main.ui.auth.login
  (:require [sablono.core :as html :refer-macros [html]]
            [lentes.core :as l]
            [cuerdas.core :as str]
            [rum.core :as rum]
            [uxbox.common.router :as rt]
            [uxbox.main.state :as st]
            [uxbox.common.rstore :as rs]
            [uxbox.main.data.auth :as da]
            [uxbox.main.data.messages :as udm]
            [uxbox.util.dom :as dom]
            [uxbox.main.ui.icons :as i]
            [uxbox.main.ui.messages :as uum]
            [uxbox.main.ui.navigation :as nav]
            [uxbox.common.ui.mixins :as mx]))

(defn- login-submit
  [event local]
  (dom/prevent-default event)
  (let [form (:form @local)]
    (rs/emit! (da/login {:username (:email form)
                         :password (:password form)}))))

(defn- login-submit-enabled?
  [local]
  (let [form (:form @local)]
    (and (not (str/empty? (:email form "")))
         (not (str/empty? (:password form ""))))))

(defn- login-field-change
  [local field event]
  (let [value (str/trim (dom/event->value event))]
    (swap! local assoc-in [:form field] value)))

(defn- login-page-render
  [own local]
  (let [on-submit #(login-submit % local)
        submit-enabled? (login-submit-enabled? local)
        form (:form @local)]
    (html
     [:div.login
      [:div.login-body
       (uum/messages)
       [:a i/logo]
       [:form {:on-submit on-submit}
        [:div.login-content
         [:input.input-text
          {:name "email"
           :ref "email"
           :value (:email form "")
           :on-change #(login-field-change local :email %)
           :placeholder "Email or Username"
           :type "text"}]
         [:input.input-text
          {:name "password"
           :ref "password"
           :value (:password form "")
           :on-change #(login-field-change local :password %)
           :placeholder "Password"
           :type "password"}]
         [:input.btn-primary
          {:name "login"
           :class (when-not submit-enabled? "btn-disabled")
           :disabled (not submit-enabled?)
           :value "Continue"
           :type "submit"}]
         [:div.login-links
          [:a {:on-click #(rt/go :auth/recovery-request)} "Forgot your password?"]
          [:a {:on-click #(rt/go :auth/register)} "Don't have an account?"]]]]]])))

(defn- login-page-will-mount
  [own]
  (when @st/auth-l
    (rt/go :dashboard/projects))
  own)

(def login-page
  (mx/component
   {:render #(login-page-render % (:rum/local %))
    :will-mount login-page-will-mount
    :name "login-page"
    :mixins [(mx/local)]}))