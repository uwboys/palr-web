(ns palr.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [palr.core-test]))

(doo-tests 'palr.core-test)
