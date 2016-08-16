JDK=`echo /Library/Java/JavaVirtualMachines/jdk1.8.0_*.jdk/Contents/Home`

if [ -d "$JDK" ]
then
    export JAVA_HOME=$JDK
    echo "Found JDK $JDK"
else
    echo "Cannot locate JDK in /Library/Java/JavaVirtualMachines"
fi
if [ -d $HOME/Eclipse/apache-maven-3.2.3 ]
then
    export M2_HOME=$HOME/Eclipse/apache-maven-3.2.3
else
    export M2_HOME=$HOME/Eclipse/apache-maven-3.3.3
fi

export PATH="$M2_HOME/bin:$JAVA_HOME/bin:$PATH"

export CSS_REPO=file:$HOME/git/org.csstudio.sns/repository/target/repository

