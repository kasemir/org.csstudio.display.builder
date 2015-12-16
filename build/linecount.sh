# Count lines of essential plugins

cd ..
for dir in org.csstudio.display.builder.editor \
           org.csstudio.display.builder.editor.rcp \
           org.csstudio.display.builder.model \
           org.csstudio.display.builder.rcp \
           org.csstudio.display.builder.representation \
           org.csstudio.display.builder.representation.javafx \
           org.csstudio.display.builder.representation.swt \
           org.csstudio.display.builder.runtime \
           org.csstudio.display.builder.util \
           org.csstudio.javafx.rtplot
do
    cd $dir
    echo -n $dir
    wc -l `find . -name *.java` | fgrep total | sed 's/total//'
    cd ..
done
