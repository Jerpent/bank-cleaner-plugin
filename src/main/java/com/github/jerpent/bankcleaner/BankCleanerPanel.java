package com.github.jerpent.bankcleaner;

import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

public class BankCleanerPanel extends PluginPanel
{
	private final BankCleanerPlugin plugin;
	private final BankCleanerConfig config;

	private final JLabel statusLabel = new JLabel("Open your bank to scan.");
	private final JPanel sellSection = new JPanel();
	private final JPanel dropSection = new JPanel();
	private final JPanel degradeSection = new JPanel();

	private boolean showIgnored = false;
	private List<RedundantItem> lastItems;
	private boolean lastShowUntradeables;
	private boolean lastShowDegradeables;

	public BankCleanerPanel(BankCleanerPlugin plugin, BankCleanerConfig config)
	{
		this.plugin = plugin;
		this.config = config;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		controls.setAlignmentX(Component.LEFT_ALIGNMENT);

		JToggleButton toggleIgnored = new JToggleButton("Show Ignored");
		toggleIgnored.setFont(toggleIgnored.getFont().deriveFont(10f));
		toggleIgnored.addActionListener(e ->
		{
			showIgnored = toggleIgnored.isSelected();
			if (lastItems != null)
			{
				SwingUtilities.invokeLater(() -> renderItems(lastItems, lastShowUntradeables, lastShowDegradeables));
			}
		});

		JButton clearIgnored = new JButton("Clear Ignored");
		clearIgnored.setFont(clearIgnored.getFont().deriveFont(10f));
		clearIgnored.addActionListener(e ->
		{
			plugin.clearIgnoredItems();
			if (lastItems != null)
			{
				lastItems = lastItems.stream()
					.map(i -> new RedundantItem(i.getItemId(), i.getName(), i.getReason(),
						i.getDominatedBy(), i.getStatComparison(), i.getCategory(), false))
					.collect(Collectors.toList());
				SwingUtilities.invokeLater(() -> renderItems(lastItems, lastShowUntradeables, lastShowDegradeables));
			}
		});

		controls.add(toggleIgnored);
		controls.add(clearIgnored);

		statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(statusLabel);
		add(Box.createVerticalStrut(4));
		add(controls);
		add(Box.createVerticalStrut(10));

		add(buildSection("Sell", Color.decode("#c0392b"), sellSection));
		add(Box.createVerticalStrut(8));
		add(buildSection("Drop / Destroy", Color.decode("#e67e22"), dropSection));
		add(Box.createVerticalStrut(8));
		add(buildSection("Degradeables (Review)", Color.decode("#2980b9"), degradeSection));
	}

	private JPanel buildSection(String title, Color color, JPanel contentPanel)
	{
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
		wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel header = new JLabel(title);
		header.setForeground(color);
		header.setFont(header.getFont().deriveFont(Font.BOLD));
		header.setAlignmentX(Component.LEFT_ALIGNMENT);
		wrapper.add(header);

		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		wrapper.add(contentPanel);

		return wrapper;
	}

	public void update(List<RedundantItem> items, boolean showUntradeables, boolean showDegradeables)
	{
		lastItems = items;
		lastShowUntradeables = showUntradeables;
		lastShowDegradeables = showDegradeables;
		SwingUtilities.invokeLater(() -> renderItems(items, showUntradeables, showDegradeables));
	}

	private void renderItems(List<RedundantItem> items, boolean showUntradeables, boolean showDegradeables)
	{
		sellSection.removeAll();
		dropSection.removeAll();
		degradeSection.removeAll();

		int sellCount = 0, dropCount = 0, degradeCount = 0;

		for (RedundantItem item : items)
		{
			if (item.isIgnored() && !showIgnored) continue;

			switch (item.getCategory())
			{
				case SELL:
					addRow(sellSection, item);
					sellCount++;
					break;
				case DROP:
					if (showUntradeables)
					{
						addRow(dropSection, item);
						dropCount++;
					}
					break;
				case DEGRADEABLE:
					if (showDegradeables)
					{
						addRow(degradeSection, item);
						degradeCount++;
					}
					break;
			}
		}

		if (sellCount == 0) addEmpty(sellSection);
		if (showUntradeables && dropCount == 0) addEmpty(dropSection);
		if (showDegradeables && degradeCount == 0) addEmpty(degradeSection);

		int total = sellCount + dropCount + degradeCount;
		statusLabel.setText(total == 0 ? "No redundant items found." : total + " item(s) flagged.");

		revalidate();
		repaint();
	}

	private void addRow(JPanel panel, RedundantItem item)
	{
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
		row.setAlignmentX(Component.LEFT_ALIGNMENT);

		// Build tooltip text — read config live so toggling takes effect without re-scan
		String tooltipText = null;
		if (!item.isIgnored() && item.getReason() != null)
		{
			tooltipText = (config.showStatComparison() && item.getStatComparison() != null)
				? item.getStatComparison()
				: item.getReason();
		}

		// Name line
		String nameText = item.isIgnored()
			? "<html><strike>" + item.getName() + "</strike></html>"
			: "- " + item.getName();
		JLabel nameLabel = new JLabel(nameText);
		if (item.isIgnored()) nameLabel.setForeground(Color.GRAY);
		nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		nameLabel.setToolTipText(tooltipText);

		row.add(nameLabel);

		// Reason sub-line (only when not ignored)
		if (item.getReason() != null && !item.isIgnored())
		{
			JLabel reasonLabel = new JLabel("  " + item.getReason());
			reasonLabel.setForeground(Color.GRAY);
			reasonLabel.setFont(reasonLabel.getFont().deriveFont(10f));
			reasonLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			reasonLabel.setToolTipText(tooltipText);
			row.add(reasonLabel);
			addPopupListener(reasonLabel, item);
		}

		addPopupListener(nameLabel, item);
		addPopupListener(row, item);

		panel.add(row);
		panel.add(Box.createVerticalStrut(4));
	}

	private void addPopupListener(JComponent component, RedundantItem item)
	{
		JPopupMenu menu = new JPopupMenu();
		if (item.isIgnored())
		{
			JMenuItem unignore = new JMenuItem("Unignore");
			unignore.addActionListener(e ->
			{
				plugin.unignoreItem(item.getItemId());
				updateIgnoreState(item.getItemId(), false);
			});
			menu.add(unignore);
		}
		else
		{
			JMenuItem ignore = new JMenuItem("Ignore");
			ignore.addActionListener(e ->
			{
				plugin.ignoreItem(item.getItemId());
				updateIgnoreState(item.getItemId(), true);
			});
			menu.add(ignore);
		}

		component.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (e.isPopupTrigger()) menu.show(e.getComponent(), e.getX(), e.getY());
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger()) menu.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	private void updateIgnoreState(int itemId, boolean ignored)
	{
		if (lastItems == null) return;
		lastItems = lastItems.stream()
			.map(i -> i.getItemId() == itemId
				? new RedundantItem(i.getItemId(), i.getName(), i.getReason(),
					i.getDominatedBy(), i.getStatComparison(), i.getCategory(), ignored)
				: i)
			.collect(Collectors.toList());
		SwingUtilities.invokeLater(() -> renderItems(lastItems, lastShowUntradeables, lastShowDegradeables));
	}

	private void addEmpty(JPanel panel)
	{
		JLabel none = new JLabel("None");
		none.setForeground(Color.GRAY);
		none.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(none);
	}

	public void reset()
	{
		SwingUtilities.invokeLater(() ->
		{
			sellSection.removeAll();
			dropSection.removeAll();
			degradeSection.removeAll();
			statusLabel.setText("Open your bank to scan.");
			lastItems = null;
			revalidate();
			repaint();
		});
	}
}
