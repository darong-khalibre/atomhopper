(defproject atomhopper "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 ; atompub client
                 [org.apache.abdera/abdera-client "1.1.3"]
                 [org.clojure/test.check "0.7.0"]
                 [clj-time "0.10.0"]]

  :target-path "target/%s"
  :source-paths ["src/clojure"]
  :test-paths ["test/clojure"]

  :java-source-paths ["src/java"]
  :javac-options ["-target" "1.6" "-source" "1.6"]

  :profiles {:uberjar {:aot :all}})
