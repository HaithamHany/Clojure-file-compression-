(ns compress)

(defn compress-file []
  (println "Please enter a file name:")
  (let [filename (read-line)
        content (try
                  (slurp filename)
                  (catch Exception e
                    (println "Oops: Specified file doesn't exist")
                    nil))]
    (when content
      (let [sentence content
            frequency-text (slurp "frequency.txt")
            frequency-words (clojure.string/split frequency-text #" ")
            unique-words (->> frequency-words
                              (map clojure.string/lower-case)
                              distinct)
            word-index-map (into {} (map-indexed (fn [index word] [word index]) unique-words))
            formatted-text (-> sentence
                               (clojure.string/replace #"(\b\d+\b)" "@$1@") ; Replacing numbers with @number@
                               (clojure.string/replace #"(\b\d+\.\d+)\b" "$$1") ; Escape numbers with decimal points
                               (clojure.string/replace #"(\p{Punct})" " $1 ") ; Add spaces around punctuation and symbols
                               (clojure.string/replace #"(\s)(\()" "$1 $2") ; Add space before opening parentheses
                               (clojure.string/replace #"\(\s" "(") ; Remove space after opening parentheses
                               (clojure.string/replace #"(\))(\s)" "$1 $2") ; Add space after closing parentheses
                               (clojure.string/replace #"\b\s*@(\d+)\s*\b" "@$1") ; Remove spaces before and after @ symbol when it's enclosing a number
                               (clojure.string/replace #"@\s+(\d+)\s+@" "@$1@")) ; Remove spaces between @ symbols for numbers
            compressed-sentence (clojure.string/join " " (map #(or (get word-index-map (clojure.string/lower-case %)) %) (clojure.string/split formatted-text #"\s+")))
            compressed-file (str filename ".ct")]
        (spit compressed-file compressed-sentence)
        (println "Compression complete.")))))


;Option 4 - decompress
(defn decompress-file []
  (println "Please enter the compressed file name:")
  (let [filename (read-line)
        content (try
                  (slurp filename)
                  (catch Exception e
                    (println "Oops: Specified file doesn't exist")
                    nil))]
    (when content
      (let [compressed-text content
            frequency-text (slurp "frequency.txt")
            frequency-words (clojure.string/split frequency-text #" ")
            unique-words (->> frequency-words
                              (map clojure.string/lower-case)
                              distinct)
            word-index-map (into {} (map-indexed (fn [index word] [index word]) unique-words))
            compressed-words (clojure.string/split compressed-text #"\s+")
            decompressed-words (map #(if (re-matches #"\d+" %)
                                       (or
                                         (get word-index-map (Integer/parseInt %)) %)
                                       %)
                                    compressed-words)
            decompressed-text (clojure.string/join " " decompressed-words)
            formatted-text (-> decompressed-text
                               (clojure.string/replace #"(?i)(?<=^|\.\s|[?!]\s)\p{L}" #(clojure.string/upper-case %)) ; Capitalize the first letter
                               (clojure.string/replace #"\(\s" "( ") ; Add space before opening parentheses
                               (clojure.string/replace #"\(\s+([^\)])" "( $1") ; Add space after opening parentheses if not followed by closing parentheses
                               (clojure.string/replace #"\(\s+(\))" " $1") ; Remove space before closing parentheses
                               (clojure.string/replace #"\)\s" ") ") ; Add space after closing parentheses
                               (clojure.string/replace #"\s?-\s?" " - ") ; Add space before and after dashes
                               (clojure.string/replace #"\s@" " @") ; Add space before @ symbol
                               (clojure.string/replace #"\$\s" "$") ; Remove space after dollar sign
                               (clojure.string/replace #"\s+\@" "@") ; Remove spaces between @ symbols
                               (clojure.string/replace #"\b\s+" " ") ; Remove extra spaces
                               (clojure.string/replace #"(?<=\p{Punct})(?<!@)\s+" " ") ; Remove extra spaces after punctuation except @ symbol
                               (clojure.string/replace #"\s+(?=[.,?!])" "") ; Remove spaces before punctuation
                               (clojure.string/replace #"(?<!@)@(\d+)@" " $1") ; Add space before the number
                               (clojure.string/replace #"\s@(\w)" " @ $1") ; Add space before individual single @
                               (clojure.string/replace #"(?<=\w)@" " @") ; Add space before individual @ at the beginning of a word
                               (clojure.string/replace #"\(\s+([^\(\)]+)\s+\)" "($1)") ;removing space in ()
                               (clojure.string/replace #"\[\s+([^\]]+)\s+\]" "[$1]") ; removing space in []
                               (clojure.string/replace #"(?<=@)\s" ""))] ; Remove space on the right of @ symbols

        (println "Decompressed text:")
        (println formatted-text)))))