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
                       [basscss-color-forms "^3.0.2"]
                       [react-progress-bar-plus "^1.2.0"]
                       [alertify "^0.3.0"]
                       [socket.io-client "^1.5.1"]
                       [react-select "^1.0.0-rc.2"]
                       [classnames "^2.2.5"]
                       [react-input-autosize "^1.1.0"]
                       [react-textarea-autosize "uwboys/react-textarea-autosize#c688fb01b012e64cdc1df8edee71a17ec2e17d97"]]
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
                   [cider/cider-nrepl "0.15.0-SNAPSHOT"]]}}

  :cljsbuild
  {:builds
   {:dev
    {:source-paths ["src/cljs"]
     :figwheel     {:on-jsload "palr.core/mount-root"}
     :compiler     {:main                     palr.core
                    :output-to                "resources/public/js/compiled/app.js"
                    :output-dir               "resources/public/js/compiled/out"
                    :asset-path               "js/compiled/out"
                    :foreign-libs             [{:file "node_modules/react-progress-bar-plus/dist/react-progress-bar-plus.js"
                                                :provides ["js.react-progress-bar-plus"]} ;; need to prefix with js.
                                               {:file "node_modules/alertify/lib/alertify.js"
                                                :provides ["js.alertify"]}
                                               {:file "node_modules/socket.io-client/socket.io.js"
                                                :provides ["js.socket.io-client"]}
                                               {:file "node_modules/react-select/dist/react-select.js"
                                                :provides ["js.react-select"]}
                                               {:file "node_modules/classnames/index.js"
                                                :provides ["js.classnames"]}
                                               {:file "node_modules/react-input-autosize/dist/react-input-autosize.min.js"
                                                :provides ["js.react-input-autosize"]}
                                               {:file "node_modules/react-textarea-autosize/lib/TextareaAutosize.min.js"
                                                :provides ["js.react-textarea-autosize"]}]
                    :source-map-timestamp     true
                    :externs                  ["externs/alertify.js"
                                               "externs/socket.io-client.js"
                                               ]}}

    :prod
    {:source-paths ["src/cljs"]
     :compiler     {:main                     palr.core
                    :output-to                "resources/public/js/compiled/app.js"
                    :optimizations            :advanced
                    :foreign-libs             [{:file "node_modules/react-progress-bar-plus/dist/react-progress-bar-plus.js"
                                                :provides ["js.react-progress-bar-plus"]}
                                               {:file "node_modules/alertify/lib/alertify.js"
                                                :provides ["js.alertify"]}
                                               {:file "node_modules/socket.io-client/socket.io.js"
                                                :provides ["js.socket.io-client"]}
                                               {:file "node_modules/react-select/dist/react-select.js"
                                                :provides ["js.react-select"]}
                                               {:file "node_modules/classnames/index.js"
                                                :provides ["js.classnames"]}
                                               {:file "node_modules/react-input-autosize/dist/react-input-autosize.min.js"
                                                :provides ["js.react-input-autosize"]}
                                               {:file "node_modules/react-textarea-autosize/lib/TextareaAutosize.min.js"
                                                :provides ["js.react-textarea-autosize"]}]
                    :closure-defines           {goog.DEBUG false}
                    :pretty-print              false
                    :externs                   ["externs/alertify.js" "externs/socket.io-client.js"]}}}})
