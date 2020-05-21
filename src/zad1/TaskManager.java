package zad1;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.*;


public class TaskManager extends JFrame implements ActionListener {

    ExecutorService exec;
    static int taskCounter;
    HashMap<String, FutureTask<String>> map;
    JList<String> jList;
    DefaultListModel<String> model;
    public TaskManager(){
        model = new DefaultListModel<>();
        jList = new JList<>(model);
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(jList));

        JPanel p = new JPanel();
        JButton b = new JButton("New Task");
        b.setActionCommand("taskStart");
        b.addActionListener(this);
        p.add(b);
        b = new JButton("Stop Task");
        b.setActionCommand("taskStop");
        b.addActionListener(this);
        p.add(b);
        b = new JButton("Task Result");
        b.setActionCommand("taskResult");
        b.addActionListener(this);
        p.add(b);
        b = new JButton("Status");
        b.setActionCommand("taskStatus");
        b.addActionListener(this);
        p.add(b);
        add(p, "South");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 720);
        setVisible(true);

        this.exec = Executors.newCachedThreadPool();
        taskCounter = 0;
        map = new HashMap<>();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Method m = this.getClass().getDeclaredMethod(e.getActionCommand());
            m.invoke(this);
        } catch(Exception exc) { exc.printStackTrace(); }
    }

    public void taskStart(){
        String taskName = "Task " + taskCounter;
        FutureTask<String> task = new FutureTask<String>(new Callable<String>(){
            @Override
            public String call() throws Exception {
                int i=0;
                int taskNo = taskCounter++;
                for(; i<100; i++){
                    if(Thread.currentThread().isInterrupted()) return null;
                    Thread.sleep(300);
//                    System.out.printf("%s is %d%s ready!%n", taskName, i+1, "%");
                }
                return "I counted to 100 in 30 seconds";
            }
        });

        map.put(taskName, task);
        exec.execute(task);
        model.addElement(taskName);
    }
    public void taskStop(){
        map.get(jList.getSelectedValue()).cancel(true);
    }
    public void taskResult(){
        StringBuilder msg = new StringBuilder();
        FutureTask<String> task = map.get(jList.getSelectedValue());
        if (task.isCancelled()) msg.append("Result unavailable due to task cancellation");
        else if (task.isDone()) {
            try {
                msg.append(jList.getSelectedValue())
                   .append(": ")
                   .append(task.get());
            } catch(Exception exc) {
                msg.append(exc.getMessage());
            }
        }
        else msg.append("Task is still running");
        JOptionPane.showMessageDialog(null, msg);
    }
    public void taskStatus(){
        StringBuilder msg = new StringBuilder();
        String name = jList.getSelectedValue();
        FutureTask task = map.get(name);
        if(task.isCancelled())
            msg.append(name)
               .append(" has been cancelled");
        else if(!(task.isDone()))
            msg.append(name)
               .append(" is still running");
        else msg.append(name)
                .append(" is done! Result is waiting for you.");
        JOptionPane.showMessageDialog(null, msg);
    }
}
