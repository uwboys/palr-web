(defproject palr "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229"]
                 [reagent "0.6.0"]
                 [binaryage/devtools "0.8.2"]
                 [re-frame "0.8.0"]
                 [secretary "1.2.3"]
                 [compojure "1.5.1"]
                 [ring "1.5.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.2.1"]
                 [cljs-ajax "0.5.8"]
                 [day8.re-frame/http-fx "0.1.1"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [day8.re-frame/async-flow-fx "0.0.6"]]

  :plugins [[lein-cljsbuild "1.1.4"]
            [lein-npm "0.6.2"]]

  :npm {:dependencies [[basscss "^8.0.1"]
                       [basscss-addons "^1.0.0-beta4"]
                       [basscss-basic "^1.0.0"]
                       [postcss "^5.2.3"]
                       [postcss-cli "^2.6.0"]
                       [postcss-cssnext "^2.8.0"]
                       [postcss-import "^8.1.2"]
                       [basscss-color-forms "^3.0.2"]]
        :package {:scripts {:postcss "postcss -c postcss.config.json -o resources/public/css/site.css ./styles/index.css"
                            :postcss:watch "npm run postcss -- --watch"}}}

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"]

  :figwheel {:css-dirs ["resources/public/css"]
             :ring-handler palr.core/dev-handler}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :profiles
  {:dev
   {:dependencies [[figwheel-sidecar "0.5.7"]
                   [com.cemerick/piggieback "0.2.1"]]

    :plugins      [[lein-figwheel "0.5.7"]
                   [lein-doo "0.1.7"]
                   [cider/cider-nrepl "0.14.0-SNAPSHOT"]]}}


  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "palr.core/mount-root"}
     :compiler     {:main                 palr.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true}}

    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            palr.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}
    {:id           "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler     {:output-to     "resources/public/js/compiled/test.js"
                    :main          palr.runner
                    ;; This is a bit weird. Don't know why it works
                    :output-dir    "resources/public/js/out"
                    :optimizations :none}}]})
