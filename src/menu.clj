(ns menu (:require [compress :as compress-ns]))
(require '[clojure.java.io :as io])
(require '[clojure.core :as str])

(defn printFilesInCurrentDir []
  (let [current-dir (System/getProperty "user.dir")]
    (doseq [file (filter #(.isFile %) (file-seq (io/file current-dir)))]
      (println (.getName file)))))

(defn displayFileContents []
  (println "Please Enter a file name: ")
  (let [filename (read-line)
        content (try
                  (slurp filename)
                  (catch Exception e
                    (println "Oops: Specified file doesn't exist")
                    nil))]
    (when content
      (println "File Contents:")
      (println content))))


(defn exit-application []
  (println "Exiting app...")
  (System/exit 0))

(def options [
              printFilesInCurrentDir
              displayFileContents
              compress/compress-file
              compress/decompress-file
              exit-application
              ])


(defn printMenu [selection]
  (println "-------------------------")
  (println "*** Compression Menu ***")
  (println "-------------------------")
  (println "1. Display list of files")
  (println "2. Display file contents")
  (println "3. Compress a file")
  (println "4. Uncompress a file")
  (println "5. Exit")
  (println "")
  (println "Enter an option? ")

  (let [opt (read-line)]
    (try
      (let [selected-index (Integer/parseInt opt)
            selected-fn (get options (dec selected-index))]
        (if (and (integer? selected-index) (not= :invalid selected-index) selected-fn)
          (selected-fn)
          (println "Invalid option. Please try again.")
          ))
      (catch NumberFormatException e
        (println "Invalid option. Please try again."))))
  (printMenu 0))

(printMenu 0)